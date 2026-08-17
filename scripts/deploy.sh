#!/usr/bin/env bash
# 打包并部署 deadman-app + 管理端静态资源到远端（systemd + Nginx）。
# 用法：
#   cp deploy/deploy.conf.example deploy/deploy.conf
#   ./scripts/deploy.sh prod
#   ./scripts/deploy.sh prod --skip-package
#   ./scripts/deploy.sh prod --skip-package --jar deadman-app/target/deadman-app-1.0.0-prod.jar \
#       --frontend /path/to/dist.zip
#   SSHPASS='...' ./scripts/deploy.sh prod
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT}"

SKIP_PACKAGE=0
JAR_OVERRIDE=""
FRONTEND_DIST="${FRONTEND_DIST:-}"
PROFILE=""

CONF="${ROOT}/deploy/deploy.conf"
if [[ -f "${CONF}" ]]; then
  # shellcheck disable=SC1090
  source "${CONF}"
fi

while [[ $# -gt 0 ]]; do
  case "$1" in
    prod|dev) PROFILE="$1"; shift ;;
    --skip-package) SKIP_PACKAGE=1; shift ;;
    --jar)
      JAR_OVERRIDE="${2:-}"
      shift 2
      ;;
    --frontend)
      FRONTEND_DIST="${2:-}"
      shift 2
      ;;
    -h|--help)
      sed -n '2,12p' "${BASH_SOURCE[0]}"
      exit 0
      ;;
    *)
      echo "未知参数: $1" >&2
      exit 1
      ;;
  esac
done

PROFILE="${PROFILE:-${PACKAGE_PROFILE:-prod}}"
DEPLOY_HOST="${DEPLOY_HOST:-}"
DEPLOY_USER="${DEPLOY_USER:-root}"
DEPLOY_PORT="${DEPLOY_PORT:-22}"
DEPLOY_DIR="${DEPLOY_DIR:-/opt/deadman}"
APP_PORT="${APP_PORT:-8080}"

if [[ -z "${DEPLOY_HOST}" ]]; then
  echo "未配置 DEPLOY_HOST。请先：cp deploy/deploy.conf.example deploy/deploy.conf" >&2
  exit 1
fi
if [[ "${PROFILE}" != "prod" && "${PROFILE}" != "dev" ]]; then
  echo "环境必须是 prod 或 dev，当前: ${PROFILE}" >&2
  exit 1
fi

COMMON_OPTS=(-o StrictHostKeyChecking=accept-new -o ServerAliveInterval=30)
if [[ -n "${DEPLOY_SSH_KEY:-}" ]]; then
  COMMON_OPTS+=(-i "${DEPLOY_SSH_KEY}")
fi
if [[ -n "${DEPLOY_SSH_CONTROL:-}" ]]; then
  COMMON_OPTS+=(-o "ControlPath=${DEPLOY_SSH_CONTROL}" -o ControlMaster=no)
fi

ssh_base() {
  if [[ -n "${SSHPASS:-}" ]] && command -v sshpass >/dev/null 2>&1; then
    sshpass -e ssh "${COMMON_OPTS[@]}" -p "${DEPLOY_PORT}" "${DEPLOY_USER}@${DEPLOY_HOST}" "$@"
  else
    ssh "${COMMON_OPTS[@]}" -p "${DEPLOY_PORT}" "${DEPLOY_USER}@${DEPLOY_HOST}" "$@"
  fi
}

scp_base() {
  if [[ -n "${SSHPASS:-}" ]] && command -v sshpass >/dev/null 2>&1; then
    sshpass -e scp "${COMMON_OPTS[@]}" -P "${DEPLOY_PORT}" "$@"
  else
    scp "${COMMON_OPTS[@]}" -P "${DEPLOY_PORT}" "$@"
  fi
}

if [[ "${SKIP_PACKAGE}" -eq 1 ]]; then
  echo "==> [1/4] 跳过打包，使用已有 jar"
else
  echo "==> [1/4] 本地打包 profile=${PROFILE}"
  "${ROOT}/scripts/package.sh" "${PROFILE}"
fi

if [[ -n "${JAR_OVERRIDE}" ]]; then
  JAR="${JAR_OVERRIDE}"
else
  JAR="$(ls -1 "${ROOT}/deadman-app/target/deadman-app-"*"-${PROFILE}.jar" 2>/dev/null | grep -v '\.original$' | head -n 1 || true)"
fi
if [[ -z "${JAR}" || ! -f "${JAR}" ]]; then
  echo "未找到 jar：${JAR_OVERRIDE:-deadman-app/target/deadman-app-*-${PROFILE}.jar}" >&2
  exit 1
fi
echo "    jar: ${JAR}"

STAGE="$(mktemp -d)"
cleanup() { rm -rf "${STAGE}"; }
trap cleanup EXIT

mkdir -p "${STAGE}/bundle/certs" "${STAGE}/bundle/web"
cp "${JAR}" "${STAGE}/bundle/deadman-app.jar"
cp "${ROOT}/deploy/deadman-app.service" "${STAGE}/bundle/"
sed "s/127.0.0.1:APP_PORT/127.0.0.1:${APP_PORT}/" "${ROOT}/deploy/nginx.conf" > "${STAGE}/bundle/nginx.conf"
cp "${ROOT}/deploy/env.prod.example" "${STAGE}/bundle/env.prod.example"
cp "${ROOT}/scripts/remote-setup.sh" "${STAGE}/bundle/remote-setup.sh"
chmod +x "${STAGE}/bundle/remote-setup.sh"

CERT_DIR="${ROOT}/data/wechat-pay"
if [[ -d "${CERT_DIR}" ]]; then
  cp -f "${CERT_DIR}/"*.pem "${STAGE}/bundle/certs/" 2>/dev/null || true
fi

if [[ -n "${FRONTEND_DIST}" ]]; then
  echo "    frontend: ${FRONTEND_DIST}"
  if [[ -d "${FRONTEND_DIST}" ]]; then
    cp -R "${FRONTEND_DIST}/." "${STAGE}/bundle/web/"
  elif [[ -f "${FRONTEND_DIST}" ]]; then
    unzip -q "${FRONTEND_DIST}" -d "${STAGE}/frontend-unpack"
    if [[ -d "${STAGE}/frontend-unpack/dist" ]]; then
      cp -R "${STAGE}/frontend-unpack/dist/." "${STAGE}/bundle/web/"
    else
      cp -R "${STAGE}/frontend-unpack/." "${STAGE}/bundle/web/"
    fi
    rm -rf "${STAGE}/bundle/web/__MACOSX"
  else
    echo "前端路径不存在: ${FRONTEND_DIST}" >&2
    exit 1
  fi
  if [[ ! -f "${STAGE}/bundle/web/index.html" ]]; then
    echo "前端包中未找到 index.html" >&2
    exit 1
  fi
fi

echo "==> [2/4] 上传到 ${DEPLOY_USER}@${DEPLOY_HOST}:${DEPLOY_DIR}"
COPYFILE_DISABLE=1 tar -C "${STAGE}" -czf "${STAGE}/bundle.tar.gz" bundle
ssh_base "mkdir -p '${DEPLOY_DIR}' /tmp/deadman-deploy"
scp_base "${STAGE}/bundle.tar.gz" "${DEPLOY_USER}@${DEPLOY_HOST}:/tmp/deadman-deploy/bundle.tar.gz"

echo "==> [3/4] 远端安装 JDK / systemd / Nginx / 前端"
ssh_base "tar -C /tmp/deadman-deploy -xzf /tmp/deadman-deploy/bundle.tar.gz && \
  DEPLOY_DIR='${DEPLOY_DIR}' APP_PORT='${APP_PORT}' PROFILE='${PROFILE}' \
  bash /tmp/deadman-deploy/bundle/remote-setup.sh"

echo "==> [4/4] 健康检查"
ssh_base 'for i in $(seq 1 60); do
  if curl -fsS -o /dev/null --max-time 5 "http://127.0.0.1:'"${APP_PORT}"'/api/components"; then
    echo "backend :'"${APP_PORT}"' HTTP 200  (try $i)"
    exit 0
  fi
  sleep 2
done
echo "应用尚未就绪，查看: journalctl -u deadman-app -n 80 --no-pager" >&2
journalctl -u deadman-app -n 80 --no-pager >&2 || true
exit 1'
if [[ -n "${FRONTEND_DIST}" ]]; then
  ssh_base "curl -fsS -o /dev/null -w 'nginx :80 HTTP %{http_code}\\n' --max-time 10 'http://127.0.0.1/' || true"
fi

echo "==> 部署完成  ${DEPLOY_HOST}  profile=${PROFILE}  dir=${DEPLOY_DIR}"
echo "    公网入口依赖安全组放行 80 端口；进程管理: systemctl status deadman-app"
