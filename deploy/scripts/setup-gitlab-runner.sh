#!/usr/bin/env bash
# GitLab Runner 注册参考（在 192.168.204.111 上交互执行）
# 安装后运行: sudo gitlab-runner register
set -euo pipefail

cat <<'EOF'
=== Runner A：构建与测试（Docker executor）===
  GitLab URL:       http://192.168.204.111/
  Token:            从 GitLab → Admin → CI/CD → Runners 或项目 Settings → CI/CD → Runners 获取
  Description:      sdp-docker
  Tags:             docker
  Executor:         docker
  Default image:    eclipse-temurin:21-jdk

=== Runner B：部署（Shell executor）===
  Description:      sdp-deploy
  Tags:             sdp-deploy
  Executor:         shell
  运行用户:         gitlab-runner（需已加入 docker 组: usermod -aG docker gitlab-runner）

=== Container Registry ===
  Admin → Settings → General → Visibility → 启用 Container Registry
  镜像地址示例: 192.168.204.111:5050/<group>/sdp

=== 自签名 Registry（如 pull 失败）===
  编辑 /etc/docker/daemon.json:
  {
    "insecure-registries": ["192.168.204.111:5050"]
  }
  systemctl restart docker

验证:
  gitlab-runner list
  gitlab-runner verify
EOF
