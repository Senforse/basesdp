#!/usr/bin/env bash
set -euo pipefail

DEPLOY_ROOT="${DEPLOY_ROOT:-/opt/sdp}"
cd "${DEPLOY_ROOT}"

if [[ -f .env ]]; then
  # shellcheck disable=SC1091
  set -a
  source .env
  set +a
fi

HTTP_PORT="${SDP_HTTP_PORT:-8088}"
MAX_ATTEMPTS="${HEALTH_MAX_ATTEMPTS:-36}"
SLEEP_SEC="${HEALTH_SLEEP_SEC:-10}"

# CI 在 Job 容器内通过 docker.sock 操作宿主机 compose，127.0.0.1 指向 Job 自身而非已映射端口。
# 优先在 sdp-web 容器内探测（与 Nginx 反代路径一致）；可选 HEALTH_HOST 走宿主机端口（本地调试）。
HEALTH_HOST="${HEALTH_HOST:-}"

echo "Waiting for /actuator/health (compose exec sdp-web${HEALTH_HOST:+, fallback http://${HEALTH_HOST}:${HTTP_PORT}}) ..."

for attempt in $(seq 1 "${MAX_ATTEMPTS}"); do
  response=""
  if response="$(docker compose exec -T sdp-web wget -qO- http://127.0.0.1/actuator/health 2>/dev/null)"; then
    :
  elif [[ -n "${HEALTH_HOST}" ]]; then
    response="$(curl -sf "http://${HEALTH_HOST}:${HTTP_PORT}/actuator/health" 2>/dev/null)" || true
  fi

  if [[ -n "${response}" ]] && echo "${response}" | grep -q '"status":"UP"'; then
    echo "Health check passed."
    exit 0
  fi
  echo "Attempt ${attempt}/${MAX_ATTEMPTS} — not ready yet."
  sleep "${SLEEP_SEC}"
done

echo "Health check failed. Recent logs:"
docker compose logs --tail=80 sdp-server sdp-web || true
exit 1
