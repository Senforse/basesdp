---
name: backend-logging
description: SDP 后端三类日志（接口访问 Access / 业务审计 Audit / 错误 Error）的统一规范。在编写或修改 sdp-admin 后端 Controller/Service、新增需要审计的业务动作、处理异常、配置 logback 或排查日志/traceId 时使用。
---

# SDP 后端日志规范

SDP 后端日志分三类，各有固定职责、字段、落盘策略。新增接口或关键业务动作时必须遵守。完整背景见 `back-end/sdp-admin/ADR.MD` 第九节，验收口径见 `back-end/sdp-admin/docs/logging-acceptance-checklist.md`。

## 三类日志职责

| 类型 | logger / 文件 | 触发方式 | 落盘 |
|------|------|------|------|
| Access 访问日志 | `access.logger` → `logs/access.log` | `AccessLogInterceptor` 自动拦截 | 默认落文件；按路径白名单可选落库 `sys_api_access_log` |
| Audit 审计日志 | `audit.logger` / `sys_audit_log` | 方法上加 `@AuditLog` 注解 | 默认落库，写库失败降级为告警，不影响主流程 |
| Error 错误日志 | 类 Logger → `logs/app-error.log` | `GlobalExceptionHandler` 统一处理 | WARN=业务异常，ERROR=系统异常 |

包路径：`org.nmgyj.authentication`（audit/、web/、web/interceptor/、config/、service/、task/）。

## 关键不变量

- **不要手写 access/error 日志**：访问日志由 `AccessLogInterceptor` 统一记录，异常日志由 `GlobalExceptionHandler` 统一记录。业务代码不要在 catch 里自己打 ERROR 再抛，交给全局处理器。
- **业务动作审计走注解**，不要手工拼 `SysAuditLog`。
- **所有结构化日志必须带 `traceId`**，从 MDC 取（pattern 已含 `traceId=%X{traceId}`），不要新造 key。
- **脱敏黑名单固定**：`password`、`token`、`authorization`、`secret`、`idcard`、`mobile`（小写比较）。新增敏感字段时，同步更新 `AuditLogAspect` 与 `AccessLogInterceptor` 两处的 `SENSITIVE_KEYS`。
- **审计/错误日志不得破坏 `ApiResponse` 协议**，前端响应结构保持 `{code,message,data}`。

## 新增审计日志（最常见任务）

在 Service 或 Controller 的写操作方法上加注解：

```java
@AuditLog(action = "UPDATE", bizType = "role", bizIdSpEL = "#id", recordDiff = true)
public ApiResponse<Void> update(@PathVariable("id") Long id, @RequestBody SysRole body) { ... }
```

注解字段（见 `authentication/audit/AuditLog.java`）：

- `action`：动作，大写常量 `CREATE` / `UPDATE` / `DELETE` / `ASSIGN` 等。
- `bizType`：业务类型小写，如 `user` / `role` / `order` / `menu`。
- `bizIdSpEL`：业务主键 SpEL。可用入参名（`#id`、`#patch.id`）、`#result`、或位置变量 `#p0` / `#a0`。
- `recordDiff`：是否记录 `beforeJson`(入参快照) 与 `afterJson`(返回值)；增删改建议 `true`。

切面（`AuditLogAspect`）会自动采集 traceId、userId、username、clientIp、requestUri、httpMethod、costMs、resultCode、successFlag，并对入参/返回做脱敏与 4000 字符截断。**无需手工填这些字段**。

`resultCode` 取自返回的 `ApiResponse.getCode()`（成功=0），所以审计方法应返回 `ApiResponse`。

## 异常处理（Error 日志）

不要在业务里 try/catch 后吞掉异常或自打日志。抛出后由 `GlobalExceptionHandler` 统一记：

- `IllegalArgumentException` → WARN，`code=1`，用于业务校验失败（如"角色编码已存在"）。Controller 里现有写法是局部 catch 转 `ApiResponse.fail(ex.getMessage())`，沿用即可。
- Sa-Token `NotLoginException` → WARN `code=401`；`NotPermissionException`/`NotRoleException` → WARN `code=403`。
- 其它未预期异常 → ERROR `code=500`，落 `app-error.log`，带堆栈。

错误日志固定字段顺序：`traceId / exceptionClass / exceptionMessage / requestUri / method / resultCode / stackSnippet`。新增异常处理时复用此格式。

## 落盘与配置

logback 配置在 `sdp-server/src/main/resources/logback-spring.xml`，四个文件：`app-info.log`、`app-error.log`、`access.log`、`audit.log`，按天+大小滚动。可配置项（`application*.yml`）：

- `sdp.logging.file.path` / `retention-days`（默认 15）/ `max-file-size` / `total-size-cap`
- `sdp.logging.access.enabled` / `include-query-string` / `max-query-length` / `db-enabled` / `db-include-paths`
- `sdp.logging.archive.cron`（清理定时任务，`LogArchiveCleanupTask`）

访问日志落库性能敏感：默认 `db-enabled=false`，仅对 `db-include-paths` 白名单路径落库，劣化应控制在 10% 内。

## 自测清单

新增/修改后对照 `docs/logging-acceptance-checklist.md`：

- [ ] 同一请求的 access/audit/error 日志可用同一 `traceId` 串联
- [ ] 写操作在 `sys_audit_log` 有记录，字段齐全；`recordDiff=true` 有 before/after
- [ ] 敏感字段在日志/库中为 `******`，未明文落盘
- [ ] 审计写库失败不影响主业务，`audit.logger` 有降级告警
- [ ] ERROR 仅进 `app-error.log`，响应仍是合法 `ApiResponse`
