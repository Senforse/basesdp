# sdp-admin（后端）

Maven 多模块后端，由 **`sdp-server`** 以 Modular Monolith 方式启动。项目总览、前端、AI 方向、工程缺口见仓库根目录 **[README.MD](../../README.MD)**。

---

## 模块结构（当前仓库实测）

```
sdp-admin/                          # 根 POM：dependencyManagement + revision + flatten
├── sdp-server/                     # 唯一启动模块 · SdpServerApplication · 默认 :8080
├── sdp-common/
│   ├── sdp-common-core/
│   ├── sdp-common-security/        # 多为依赖聚合，Sa-Token 等
│   └── sdp-common-mybatis/
├── sdp-modules/
│   ├── sdp-module-system/          # 认证、RBAC、字典、组织、日志、codegen 元数据 API
│   ├── sdp-module-codegen/         # Thymeleaf 全栈代码生成模板
│   └── sdp-module-business/        # 业务域（如 order）
├── sdp-api/                        # 【空壳】Feign/DTO 预留，未启用
└── sdp-gateway/                    # 【空壳】网关预留，未启用
```

**版本**：Java 21 · Spring Boot 3.2.5 · MyBatis-Plus 3.5.8 · Sa-Token 1.44.0 · 达梦 JDBC（详见根 `pom.xml`）。

---

## 包与职责

| 包前缀 | 模块 | 说明 |
|--------|------|------|
| `org.nmgyj.authentication.*` | system | 登录、`ApiResponse`、CORS、访问/审计日志切面 |
| `org.nmgyj.system.*` | system | 用户、角色、菜单、组织、字典、在线用户等 |
| `org.nmgyj.codegen.*` | codegen | 在线建表与代码生成 |
| `org.nmgyj.<domain>.*` | business | 业务实体/Mapper/Service/Controller |

**扩展约定**

- 新业务放在 `sdp-module-business` 或新增 `sdp-module-*`，勿堆进 system。
- 权限必须在服务端落地（`@SaCheckPermission`）；全局拦截器宜沉淀到 `sdp-common-security`（部分仍为规划中）。
- 多租户 / 动态插件：设计见下文「规划预留」；**多数能力尚未落地**，以 `DESIGN_UTF8.md` 标注为准。

---

## 本地运行

**前置**：达梦、Redis（与 `application-dev.yml` 一致）、JDK 21。

```bash
cd back-end/sdp-admin
mvn -pl sdp-server -am spring-boot:run
```

- 主配置：`sdp-server/src/main/resources/application.yml`（`spring.profiles.active=dev`）
- 环境配置：`application-dev.yml`（数据源、Redis、Sa-Token）— **勿提交生产密钥**
- 库表脚本：`sdp-server/src/main/resources/db/schema-dm.sql`；RBAC：`sdp-modules/sdp-module-system/src/main/resources/db/rbac-dm.sql`

**构建 / 测试**

```bash
mvn verify                    # 全量（测试仍很少，见工程路线图 P0）
mvn -pl sdp-server -am test   # 仅 server 聚合模块
```

---

## API 文档

| 用途 | URL |
|------|-----|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |

协作：可导出 JSON 给 Postman/Apifox；前端 Mock 以 `/v3/api-docs` 为单一数据源。

统一响应：`ApiResponse` + `GlobalExceptionHandler`（细节见 `DESIGN_UTF8.md`）。

---

## 依赖与分层实践（简版）

1. **版本仲裁**：在根 `sdp-admin` 的 `<dependencyManagement>` 锁定三方版本；子模块引用内部 artefact 可省略 version。
2. **最小依赖**：DB/MyBatis 相关只经由 `sdp-common-mybatis` 与需要 DB 的模块引入。
3. **模块间调用**：将来拆服务时用 `sdp-api` 放 Feign + DTO；当前 monolith 内直接依赖 service 即可。
4. **勿过早微服务**：`sdp-api` / `sdp-gateway` 保持空壳，等业务模块做深再拆（见 [docs/ai-platform-vision.md](../../docs/ai-platform-vision.md)）。

---

## 规划预留（【规划中】，非已全部实现）

- **多租户**：`sdp-common-core` 中 `TenantContext` + `sdp-common-mybatis` 插件统一注入租户 ID（字段/分库策略后定）。
- **动态插件**：`sdp-common-core` 标准接口 + Spring Bean 扩展点；真正 ClassLoader 隔离后置。

落地状态与差距清单 → [DESIGN_UTF8.md](DESIGN_UTF8.md) 第 9 章、[docs/engineering-roadmap.md](../../docs/engineering-roadmap.md)。

---

## 后端相关文档

| 文档 | 说明 |
|------|------|
| [../../README.MD](../../README.MD) | 全项目入口（含前端、Skills、AI 摘要） |
| [DESIGN_UTF8.md](DESIGN_UTF8.md) | 管理端设计方案（认证、RBAC、达梦、API） |
| [docs/sdp-auth-rbac-design.md](docs/sdp-auth-rbac-design.md) | RBAC 表与权限模型 |
| [docs/logging-acceptance-checklist.md](docs/logging-acceptance-checklist.md) | 三类日志验收 |
| [ADR.MD](ADR.MD) | 架构决策记录 |
| [../../docs/engineering-roadmap.md](../../docs/engineering-roadmap.md) | P0 测试 / CI / Flyway |
| [../../.cursor/skills/backend-logging/SKILL.md](../../.cursor/skills/backend-logging/SKILL.md) | 日志与 `@AuditLog` 规范 |
| [../../.cursor/skills/sa-token-rbac/SKILL.md](../../.cursor/skills/sa-token-rbac/SKILL.md) | 鉴权与权限点 |
| [../../.cursor/skills/add-crud-module/SKILL.md](../../.cursor/skills/add-crud-module/SKILL.md) | 端到端新增业务模块 |

---

## 编码与文本

- 源文件 **UTF-8 无 BOM**；Windows 批量修复见 `normalize_text_encoding.py` 与根目录 Skill `utf8-encoding`。
