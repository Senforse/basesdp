# Sdp 项目

前后端分离的企业数字化平台（Spring Boot 3 + Vue 3）。

- **前端**：[`front-end/sdp-app-vue/`](front-end/sdp-app-vue/)
- **后端**：[`back-end/sdp-admin/`](back-end/sdp-admin/)
- **CI/CD 与部署**：[`deploy/README.md`](deploy/README.md)

## 快速开始

### 本地后端

```bash
cd back-end/sdp-admin
# 设置 SDP_DB_PASSWORD 或参考 sdp-server/src/main/resources/application-dev.yml.example
mvn -pl sdp-server -am spring-boot:run
```

### 本地前端

```bash
cd front-end/sdp-app-vue
npm ci && npm run dev
```

### 推送到测试环境

```bash
git push origin develop
```

流水线成功后访问：**http://192.168.204.111:8088**
