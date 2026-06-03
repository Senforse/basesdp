#!/usr/bin/env bash
set -euo pipefail

DEPLOY_ROOT="${DEPLOY_ROOT:-/opt/sdp}"
cd "${DEPLOY_ROOT}"

if [[ ! -f .env ]]; then
  echo "Missing ${DEPLOY_ROOT}/.env — copy from deploy/compose/.env.example and configure."
  exit 1
fi

# shellcheck disable=SC1091
set -a
source .env
set +a

: "${REGISTRY:?REGISTRY is required (set in .env or export before deploy)}"
: "${IMAGE_TAG:?IMAGE_TAG is required}"

if [[ -n "${CI_REGISTRY:-}" && -n "${CI_REGISTRY_USER:-}" && -n "${CI_REGISTRY_PASSWORD:-}" ]]; then
  echo "${CI_REGISTRY_PASSWORD}" | docker login -u "${CI_REGISTRY_USER}" --password-stdin "${CI_REGISTRY}"
fi

echo "Deploying ${REGISTRY}/sdp-server:${IMAGE_TAG} and ${REGISTRY}/sdp-web:${IMAGE_TAG} ..."

docker compose pull
docker compose up -d --remove-orphans

"${DEPLOY_ROOT}/health-check.sh"

echo "Deploy finished. Demo URL: http://192.168.204.111:${SDP_HTTP_PORT:-8088}"
