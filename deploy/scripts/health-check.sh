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
HEALTH_URL="http://127.0.0.1:${HTTP_PORT}/actuator/health"
MAX_ATTEMPTS="${HEALTH_MAX_ATTEMPTS:-36}"
SLEEP_SEC="${HEALTH_SLEEP_SEC:-10}"

echo "Waiting for ${HEALTH_URL} ..."

for attempt in $(seq 1 "${MAX_ATTEMPTS}"); do
  if response="$(curl -sf "${HEALTH_URL}" 2>/dev/null)" && echo "${response}" | grep -q '"status":"UP"'; then
    echo "Health check passed."
    exit 0
  fi
  echo "Attempt ${attempt}/${MAX_ATTEMPTS} — not ready yet."
  sleep "${SLEEP_SEC}"
done

echo "Health check failed. Recent logs:"
docker compose logs --tail=80 sdp-server sdp-web || true
exit 1
