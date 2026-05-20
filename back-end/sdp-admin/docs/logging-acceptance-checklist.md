# SDP 日志能力验收清单（Phase 1~3）

## 1. traceId 串联验收

- [ ] 请求头不带 `X-Trace-Id` 调用任意业务接口，响应头返回新生成 `X-Trace-Id`
- [ ] 请求头带 `X-Trace-Id=test-trace-001`，访问日志、审计日志、异常日志中均出现同一值
- [ ] 同一请求内 `access.log`、`audit.log`、`app-error.log` 可通过 `traceId` 串联
- [ ] 并发 50 请求抽检，无 traceId 串号、无 MDC 泄漏

## 2. 审计日志验收

- [ ] 执行用户创建、用户更新、角色分配、用户删除，`sys_audit_log` 有对应记录
- [ ] 记录字段覆盖：`trace_id/user_id/username/biz_type/biz_id/action/result_code/cost_ms/op_time`
- [ ] `recordDiff=true` 的方法有 `before_json` 与 `after_json`
- [ ] 审计写库失败时主流程不报错，且 `audit.logger` 有降级告警

## 3. 异常日志验收

- [ ] `IllegalArgumentException` 走 `WARN`，日志包含 `traceId/exceptionClass/requestUri/method`
- [ ] Sa-Token 权限类异常（401/403）走 `WARN` 且包含结构化字段
- [ ] 未预期异常走 `ERROR`，`app-error.log` 包含堆栈与 `traceId`
- [ ] 前端响应结构仍保持 `ApiResponse` 协议，不新增破坏性字段

## 4. 日志分文件与滚动验收

- [ ] 启动后自动生成 `app-info.log`、`app-error.log`、`access.log`、`audit.log`
- [ ] `ERROR` 级别仅落入 `app-error.log`，`app-info.log` 不重复收录 ERROR
- [ ] 滚动策略生效：按天+大小切分，保留天数按 `sdp.logging.file.retention-days`
- [ ] `traceId=%X{traceId}` 在所有文件日志 pattern 可见

## 5. 查询与归档验收

- [ ] `GET /api/admin/logs/login` 可按用户名、状态、时间范围分页查询
- [ ] `GET /api/admin/logs/access` 可按 `traceId`、URI、状态码分页查询
- [ ] `GET /api/admin/logs/audit` 可按 `bizType/bizId/action` 分页查询
- [ ] `POST /api/admin/logs/archive/cleanup` 返回各类日志删除计数
- [ ] 定时任务按 `sdp.logging.archive.cron` 执行，日志中可观测清理结果

## 6. 性能验证建议

- [ ] 基线：关闭访问日志落库（`sdp.logging.access.db-enabled=false`）测 P95/P99
- [ ] 对比：开启访问日志落库后再测 P95/P99，接口性能劣化不超过 10%
- [ ] 高峰压测期间观察 `app-error.log` 是否出现连接池耗尽、慢 SQL 或批量删除超时
- [ ] 若性能劣化明显，先下调落库范围（`db-include-paths`），再评估异步化
