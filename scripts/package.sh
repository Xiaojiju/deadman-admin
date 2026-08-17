#!/usr/bin/env bash
# 本地打包 deadman-app（默认 -DskipTests 跳过测试）。
# 用法：
#   ./scripts/package.sh            # 默认 prod（不含 pay-mock，跳过测试）
#   ./scripts/package.sh prod
#   ./scripts/package.sh dev        # 含 Mock 支付，便于联调（跳过测试）
#   ./scripts/package.sh prod --with-tests
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT}"

PROFILE="${1:-prod}"
shift || true

SKIP_TESTS=true
for arg in "$@"; do
  case "${arg}" in
    --with-tests) SKIP_TESTS=false ;;
    -h|--help)
      sed -n '2,8p' "${BASH_SOURCE[0]}"
      exit 0
      ;;
    *)
      echo "未知参数: ${arg}" >&2
      exit 1
      ;;
  esac
done

if [[ "${PROFILE}" != "prod" && "${PROFILE}" != "dev" ]]; then
  echo "环境必须是 prod 或 dev，当前: ${PROFILE}" >&2
  exit 1
fi

MVN_ARGS=(-pl deadman-app -am "-P${PROFILE}" package)
if [[ "${SKIP_TESTS}" == "true" ]]; then
  MVN_ARGS+=(-DskipTests)
fi

echo "==> 打包 deadman-app  profile=${PROFILE}  skipTests=${SKIP_TESTS}"
./mvnw "${MVN_ARGS[@]}"

JAR="$(ls -1 "${ROOT}/deadman-app/target/deadman-app-"*"-${PROFILE}.jar" 2>/dev/null | grep -v '\.original$' | head -n 1 || true)"
if [[ -z "${JAR}" || ! -f "${JAR}" ]]; then
  echo "未找到产物: deadman-app/target/deadman-app-*-${PROFILE}.jar" >&2
  exit 1
fi

echo "==> 产物: ${JAR}"
ls -lh "${JAR}"
