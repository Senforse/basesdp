#!/usr/bin/env bash
# 在测试服务器 192.168.204.111 上以 root 或 sudo 执行（一次性初始化）
set -euo pipefail

SDP_ROOT="/opt/sdp"
SDP_HTTP_PORT="${SDP_HTTP_PORT:-8088}"
GITLAB_RUNNER_USER="${GITLAB_RUNNER_USER:-gitlab-runner}"

echo "==> Sdp test server setup (independent stack, port ${SDP_HTTP_PORT})"

if ! command -v docker >/dev/null 2>&1; then
  echo "Installing Docker..."
  curl -fsSL https://get.docker.com | sh
  systemctl enable --now docker
fi

docker compose version >/dev/null

echo "==> Creating ${SDP_ROOT}"
install -d -m 755 "${SDP_ROOT}"

if [[ ! -f "${SDP_ROOT}/.env" ]]; then
  if [[ -f "${SDP_ROOT}/../sdp/deploy/compose/.env.example" ]]; then
    cp "${SDP_ROOT}/../sdp/deploy/compose/.env.example" "${SDP_ROOT}/.env"
  elif [[ -f "./deploy/compose/.env.example" ]]; then
    cp "./deploy/compose/.env.example" "${SDP_ROOT}/.env"
  else
    cat > "${SDP_ROOT}/.env" <<EOF
REGISTRY=192.168.204.111:5050/your-group/sdp
IMAGE_TAG=latest
DB_USER=sdpdba
DB_PASSWORD=changeme
SDP_HTTP_BIND=0.0.0.0
SDP_HTTP_PORT=${SDP_HTTP_PORT}
EOF
  fi
  chmod 600 "${SDP_ROOT}/.env"
  echo "Created ${SDP_ROOT}/.env — edit REGISTRY and DB_PASSWORD before first deploy."
fi

if id "${GITLAB_RUNNER_USER}" >/dev/null 2>&1; then
  usermod -aG docker "${GITLAB_RUNNER_USER}"
  chown -R "${GITLAB_RUNNER_USER}:${GITLAB_RUNNER_USER}" "${SDP_ROOT}"
  echo "Added ${GITLAB_RUNNER_USER} to docker group."
fi

if command -v firewall-cmd >/dev/null 2>&1; then
  firewall-cmd --add-port="${SDP_HTTP_PORT}/tcp" --permanent || true
  firewall-cmd --reload || true
  echo "Firewall: opened tcp/${SDP_HTTP_PORT}"
elif command -v ufw >/dev/null 2>&1; then
  ufw allow "${SDP_HTTP_PORT}/tcp" || true
fi

echo ""
echo "==> GitLab Runner (manual steps if not installed)"
echo "  1. Install: https://docs.gitlab.com/runner/install/"
echo "  2. Register Runner A: executor=docker, tag=docker"
echo "  3. Register Runner B: executor=shell, tag=sdp-deploy, user=${GITLAB_RUNNER_USER}"
echo "  4. Enable Container Registry in GitLab Admin"
echo ""
echo "==> Verify DM8 listens on 5236"
ss -tlnp | grep 5236 || echo "WARN: port 5236 not listening"
echo ""
echo "Setup complete. Demo URL after deploy: http://192.168.204.111:${SDP_HTTP_PORT}"
