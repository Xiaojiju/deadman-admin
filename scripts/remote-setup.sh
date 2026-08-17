#!/usr/bin/env bash
# 在目标服务器上执行：安装 JDK 21、落地 jar / env / systemd / Nginx 并重启。
# 由 scripts/deploy.sh 上传后调用，也可手动：
#   DEPLOY_DIR=/opt/deadman APP_PORT=8080 bash remote-setup.sh
set -euo pipefail

BUNDLE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DEPLOY_DIR="${DEPLOY_DIR:-/opt/deadman}"
APP_PORT="${APP_PORT:-8080}"
PROFILE="${PROFILE:-prod}"

echo "[remote] 安装目录 ${DEPLOY_DIR}"
mkdir -p "${DEPLOY_DIR}/certs" "${DEPLOY_DIR}/logs" "${DEPLOY_DIR}/data" "${DEPLOY_DIR}/web"
# Nginx 以 nginx 用户读静态资源，目录需可遍历；密钥文件保持 600
chmod 755 "${DEPLOY_DIR}"

install_jdk21() {
  if command -v java >/dev/null 2>&1; then
    if java -version 2>&1 | grep -Eq 'version "21'; then
      echo "[remote] 已存在 JDK 21"
      return
    fi
  fi
  echo "[remote] 安装 Temurin JDK 21（清华镜像）"
  mkdir -p /usr/lib/jvm
  local tarball="/tmp/temurin-21.tar.gz"
  curl -fL --retry 3 -o "${tarball}" \
    "https://mirrors.tuna.tsinghua.edu.cn/Adoptium/21/jdk/x64/linux/OpenJDK21U-jdk_x64_linux_hotspot_21.0.12_8.tar.gz"
  rm -rf /usr/lib/jvm/temurin-21
  mkdir -p /usr/lib/jvm/temurin-21
  tar -xzf "${tarball}" -C /usr/lib/jvm/temurin-21 --strip-components=1
  ln -sfn /usr/lib/jvm/temurin-21/bin/java /usr/bin/java
}

install_jdk21
JAVA_HOME_VALUE=""
if [[ -x /usr/lib/jvm/temurin-21/bin/java ]]; then
  JAVA_HOME_VALUE=/usr/lib/jvm/temurin-21
elif command -v java >/dev/null 2>&1; then
  JAVA_HOME_VALUE="$(dirname "$(dirname "$(readlink -f "$(command -v java)")")")"
fi

if [[ -d "${BUNDLE_DIR}/certs" ]]; then
  cp -f "${BUNDLE_DIR}/certs/"*.pem "${DEPLOY_DIR}/certs/" 2>/dev/null || true
  chmod 600 "${DEPLOY_DIR}/certs/"*.pem 2>/dev/null || true
fi

if [[ -f "${BUNDLE_DIR}/web/index.html" ]]; then
  echo "[remote] 更新前端静态资源"
  rm -rf "${DEPLOY_DIR}/web"
  mkdir -p "${DEPLOY_DIR}/web"
  cp -R "${BUNDLE_DIR}/web/." "${DEPLOY_DIR}/web/"
  chmod -R a+rX "${DEPLOY_DIR}/web"
fi

if [[ -f "${DEPLOY_DIR}/deadman-app.jar" ]]; then
  cp -a "${DEPLOY_DIR}/deadman-app.jar" "${DEPLOY_DIR}/deadman-app.jar.bak"
fi
cp -f "${BUNDLE_DIR}/deadman-app.jar" "${DEPLOY_DIR}/deadman-app.jar"

if [[ ! -f "${DEPLOY_DIR}/env" ]]; then
  echo "[remote] 首次写入 ${DEPLOY_DIR}/env ，请随后补全密钥"
  cp "${BUNDLE_DIR}/env.prod.example" "${DEPLOY_DIR}/env"
  if [[ -f /root/.deadman-infra.env ]]; then
    echo "" >> "${DEPLOY_DIR}/env"
    echo "# 来自 /root/.deadman-infra.env" >> "${DEPLOY_DIR}/env"
    cat /root/.deadman-infra.env >> "${DEPLOY_DIR}/env"
  fi
fi

if [[ -n "${JAVA_HOME_VALUE}" ]]; then
  if grep -q '^JAVA_HOME=' "${DEPLOY_DIR}/env"; then
    sed -i "s|^JAVA_HOME=.*|JAVA_HOME=${JAVA_HOME_VALUE}|" "${DEPLOY_DIR}/env"
  else
    printf '\nJAVA_HOME=%s\n' "${JAVA_HOME_VALUE}" >> "${DEPLOY_DIR}/env"
  fi
fi
if ! grep -q '^SPRING_PROFILES_ACTIVE=' "${DEPLOY_DIR}/env"; then
  echo "SPRING_PROFILES_ACTIVE=${PROFILE}" >> "${DEPLOY_DIR}/env"
fi
if ! grep -q '^SERVER_PORT=' "${DEPLOY_DIR}/env"; then
  echo "SERVER_PORT=${APP_PORT}" >> "${DEPLOY_DIR}/env"
fi
chmod 600 "${DEPLOY_DIR}/env"

cp -f "${BUNDLE_DIR}/deadman-app.service" /etc/systemd/system/deadman-app.service
sed -i "s|/opt/deadman|${DEPLOY_DIR}|g" /etc/systemd/system/deadman-app.service

if [[ -d /etc/nginx/conf.d ]]; then
  if grep -qE '^\s*listen\s+80' /etc/nginx/nginx.conf; then
    echo "[remote] 注释 nginx.conf 中自带的 listen 80，避免与 conf.d 冲突"
    cp -a /etc/nginx/nginx.conf "/etc/nginx/nginx.conf.bak.$(date +%s)"
    sed -i -E 's/^(\s*listen\s+\[::\]:80)/# \1/' /etc/nginx/nginx.conf
    sed -i -E 's/^(\s*listen\s+80)/# \1/' /etc/nginx/nginx.conf
  fi
  if [[ -f /etc/nginx/conf.d/default.conf ]]; then
    mv -f /etc/nginx/conf.d/default.conf /etc/nginx/conf.d/default.conf.disabled
  fi
  cp -f "${BUNDLE_DIR}/nginx.conf" /etc/nginx/conf.d/deadman.conf
  nginx -t
  systemctl enable nginx
  systemctl reload nginx || systemctl restart nginx
fi

systemctl daemon-reload
systemctl enable deadman-app
systemctl restart deadman-app

echo "[remote] 等待进程起来..."
for i in $(seq 1 30); do
  if systemctl is-active --quiet deadman-app; then
    break
  fi
  sleep 1
done
systemctl --no-pager --full status deadman-app | head -n 20
echo "[remote] 安装完成"
