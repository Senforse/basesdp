# Sdp CI/CD 部署指南

测试服务器：`192.168.204.111`（GitLab + 达梦 DM8 + Sdp 独立 Docker 栈）

## 独立环境说明（不干扰宿主机 Redis / Nginx）

测试服务器上若已有 **Redis**、**Nginx**（例如 GitLab、其他业务），Sdp **不会修改或复用**它们：

| 组件 | 宿主机已有服务 | Sdp 独立环境 |
|------|----------------|--------------|
| Redis | 宿主机 `6379` 等 | 容器 `sdp-redis`，**不映射**宿主机端口，仅 `sdp-internal` 网络内可达 |
| Nginx / Web | 宿主机 **80/443** | 容器 `sdp-web` 自带 Nginx，映射 **8088→80**（可改 `.env`） |
| 后端 API | — | 容器 `sdp-server`，**不映射**宿主机 8080 |
| 数据库 | 宿主机达梦 DM8 | 容器经 `host.docker.internal:5236` 连接，只读使用现有库 |

**演示地址（默认）：** http://192.168.204.111:8088

**Docker 资源命名：** 网络 `sdp-internal`、数据卷 `sdp-redis-data`、容器名 `sdp-redis` / `sdp-server` / `sdp-web`，与宿主机及其他 Compose 项目隔离。

```
宿主机
├── 已有 Redis :6379          ← 不动
├── 已有 Nginx :80/:443       ← 不动
├── 达梦 DM8 :5236            ← Sdp 只连接，不安装
└── Docker 网络 sdp-internal
    ├── sdp-redis（内部 6379）
    ├── sdp-server（内部 8080）
    └── sdp-web → 宿主机 :8088
```

## 日常开发流程

1. 本地开发并自测（设置 `SDP_DB_PASSWORD` 环境变量，或参考 `application-dev.yml.example`）。
2. 提交并推送 `develop` 分支：

```bash
git add .
git commit -m "feat: your change"
git push origin develop
```

3. 在 GitLab → CI/CD → Pipelines 查看流水线。
4. 流水线成功后，浏览器访问：**http://192.168.204.111:8088**

## 流水线阶段

| Stage | 触发 | 说明 |
|-------|------|------|
| validate | 所有分支 push | 前端 `npm run build` |
| test | 所有分支 push | 后端 `mvn test` |
| build | 仅 `develop` | 构建并推送 Docker 镜像到 GitLab Registry |
| deploy | 仅 `develop` | 在测试服务器 `/opt/sdp` 执行 `docker compose up` |

## 一次性：测试服务器初始化

在 **192.168.204.111** 上以 root 或 sudo 执行。

**快捷脚本（推荐）：**

```bash
# 在仓库 clone 目录或上传脚本后执行
sudo bash deploy/scripts/setup-test-server.sh
sudo bash deploy/scripts/setup-gitlab-runner.sh   # 查看 Runner 注册说明
```

**本地 Git 远程与 develop 分支（Windows）：**

```powershell
.\deploy\scripts\setup-local-git.ps1 -GitLabUrl "http://192.168.204.111/<组>/sdp.git"
```

详见 [`GITLAB_SETUP.md`](GITLAB_SETUP.md)。

### 1. 安装 Docker

```bash
curl -fsSL https://get.docker.com | sh
systemctl enable --now docker
docker compose version
```

### 2. 启用 GitLab Container Registry

GitLab Admin → Settings → General → Visibility → 启用 Container Registry。

Registry 地址示例：`192.168.204.111:5050`（以实际 GitLab 配置为准）。

### 3. 安装 GitLab Runner

```bash
curl -L https://packages.gitlab.com/install/repositories/runner/gitlab-runner/script.rpm.sh | bash
# Debian/Ubuntu 用 script.deb.sh
gitlab-runner register
```

注册 **两个** Runner：

**Runner A — Docker 构建（validate / test / build）**

- Executor: `docker`
- Tags: `docker`
- Default image: `eclipse-temurin:21-jdk`

**Runner B — 本机部署（deploy）**

- Executor: `shell`
- Tags: `sdp-deploy`
- 运行用户需有 Docker 权限：`sudo usermod -aG docker gitlab-runner`

### 4. 创建部署目录

```bash
sudo mkdir -p /opt/sdp
sudo cp deploy/compose/.env.example /opt/sdp/.env
sudo chmod 600 /opt/sdp/.env
sudo chown -R gitlab-runner:gitlab-runner /opt/sdp
```

编辑 `/opt/sdp/.env`：

```bash
REGISTRY=192.168.204.111:5050/<你的组>/sdp
IMAGE_TAG=latest
DB_USER=sdpdba
DB_PASSWORD=<达梦密码>
SDP_HTTP_BIND=0.0.0.0
SDP_HTTP_PORT=8088
```

> **勿将 `SDP_HTTP_PORT` 设为 80**，以免与宿主机 Nginx 冲突。若 8088 已被占用，可改为 `8089` 等未使用端口。

### 5. 配置 GitLab CI/CD Variables

项目 **Settings → CI/CD → Variables**（可选，DB 密码已在 `/opt/sdp/.env` 中维护）：

| 变量 | 说明 |
|------|------|
| `DB_USER` | 达梦用户名（若不在 .env 维护） |
| `DB_PASSWORD` | 达梦密码（Masked） |

### 6. 防火墙

仅开放 Sdp 演示端口（**不要**为 Sdp 改宿主机 Nginx/Redis 相关规则）：

```bash
firewall-cmd --add-port=8088/tcp --permanent && firewall-cmd --reload
# 若 .env 中改了 SDP_HTTP_PORT，开放对应端口
```

### 7. 达梦数据库连通性

确认 DM8 监听 `5236`，且容器可通过 `host.docker.internal` 访问：

```bash
ss -tlnp | grep 5236
```

若 DM 仅监听 `127.0.0.1`，需调整 DM 配置或使用 `network_mode: host`（需改 compose，联系 DBA）。

### 8. 本地添加 GitLab 远程

```bash
git remote add origin http://192.168.204.111/<组>/sdp.git
git checkout -b develop
git push -u origin develop
```

## 本地 Docker 验证（可选）

在仓库根目录，先配置 `deploy/compose/.env`（勿提交），然后：

```bash
cd deploy/compose
docker compose --env-file .env up -d --build
```

## 回滚

在测试服务器：

```bash
cd /opt/sdp
# 修改 .env 中 IMAGE_TAG 为上一个 commit SHA
vim .env
docker compose pull && docker compose up -d
```

## 目录说明

```
deploy/
├── docker/
│   ├── Dockerfile.backend    # 后端镜像（构建上下文：back-end/sdp-admin）
│   ├── Dockerfile.frontend   # 前端镜像（构建上下文：仓库根目录）
│   └── nginx.conf
├── compose/
│   ├── docker-compose.yml
│   └── .env.example
└── scripts/
    ├── deploy.sh
    └── health-check.sh
```

## 常见问题

| 现象 | 处理 |
|------|------|
| Pipeline build 失败：找不到达梦驱动 | 确认 Maven 私服可解析 `DmJdbcDriver18`，或在 Dockerfile 中增加 `mvn install:install-file` |
| deploy 失败：Runner 无 sdp-deploy tag | 检查 shell Runner 已注册且 tag 匹配 |
| 健康检查超时 | `docker compose logs sdp-server` 查看 DM/Redis 连接错误 |
| 8088 无法访问 | 检查防火墙、`SDP_HTTP_PORT`、是否与宿主机 Nginx 80 混淆（Sdp 走 8088） |
| 与宿主机 Redis 冲突 | Sdp Redis 未映射宿主机端口；若仍冲突，检查是否有其他 compose 占用同名容器 `sdp-redis` |
| Registry 证书错误 | Docker 配置 `insecure-registries` 指向 GitLab Registry 地址 |
