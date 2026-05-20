# 架构决策记录（ADR - Architecture Decision Records） 
这对于“从零构建”的新平台极为重要。在项目根目录建一个 `docs/adr/` 文件夹。用极其简短的 Markdown 记录你们的技术决策。 _例如：《001-为什么 SDP 平台选择 Redis Opaque Token 而不是 JWT.md》。_ 当未来有人质疑你的技术选型时，直接把这个文件甩给他。


# 日志架构设计

## 一、微服务日志体系的核心目标

后端日志本质上解决 5 个问题：

目标	说明
问题排查	为什么订单失败？为什么任务卡死？
审计追踪	谁改了工单？谁删除了数据？
性能分析	哪个接口慢？SQL耗时多少？
分布式追踪	一个请求经过了哪些服务？
运维监控	错误率、异常量、系统状态

## 二、现代 Java 微服务日志最佳实践（推荐方案）

现在 Java 微服务领域的主流方案：

SLF4J（日志门面）
    ↓
Logback（日志实现）
    ↓
ELK / Loki（日志收集）
    ↓
Kibana / Grafana（日志查询）

## 三 SpringBoot 为什么默认使用 Logback？

因为：

Spring 官方默认支持
配置简单
性能稳定
社区生态最好
与 SLF4J 集成最佳

所以：

绝大多数 SpringBoot 微服务项目默认就是：

SLF4J + Logback

## 四、工业级日志架构

应用服务
   ↓
SLF4J API
   ↓
Logback
   ↓
JSON结构化日志
   ↓
Filebeat / Promtail
   ↓
ELK 或 Loki
   ↓
Kibana / Grafana

八、MES 系统建议的日志分类

这是重点。

很多项目最大问题：

所有日志混在一起。

后期完全不可维护。

1. 接口访问日志

记录：

URL
参数
IP
用户
耗时
返回值

通常用：

Spring AOP
Filter
Interceptor

实现。

2. 业务审计日志（MES 非常重要）

MES 必须有：

工单修改
状态变更
设备操作
生产参数修改
BOM修改

例如：

张三修改了工单 WO20260501
旧值：待生产
新值：生产中

这是：

Audit Log（审计日志）

工业系统核心能力之一。

3. 错误日志

记录：

Exception
stacktrace
traceId
请求参数

必须单独文件。

## 九、SDP 应用内日志标准（2026-05）

### 1) 日志分类与职责

- 接口访问日志（Access Log）：记录每次 HTTP 入站请求的基础行为轨迹，用于问题排查与性能分析。
- 业务审计日志（Audit Log）：记录关键业务动作与变更上下文，用于审计追踪与责任归属。
- 错误日志（Error Log）：记录业务异常与系统异常，用于快速定位故障根因。

### 2) 字段标准

公共字段（Access/Audit/Error 通用）：

- `traceId`：请求链路唯一标识，来自 `X-Trace-Id` 或网关/服务端生成。
- `userId`：当前登录用户主键，未登录场景可为空。
- `username`：当前用户标识，未知场景可为空字符串。
- `clientIp`：客户端 IP（优先 `X-Forwarded-For`）。
- `requestUri`：请求路径（不含域名）。
- `method`：HTTP 方法（GET/POST/...）。
- `costMs`：请求或操作耗时（毫秒）。
- `resultCode`：业务返回码或 HTTP 状态语义码。

审计扩展字段（Audit）：

- `bizType`、`bizId`、`action`、`beforeJson`、`afterJson`。

错误扩展字段（Error）：

- `exceptionClass`、`exceptionMessage`、`stackSnippet`。

### 3) 脱敏与安全策略

- 默认脱敏字段黑名单：`password`、`token`、`authorization`、`secret`、`idCard`、`mobile`。
- 黑名单字段在日志中仅保留掩码值（例如 `******`），禁止明文落盘、落库。
- 对 query/body 采用长度上限与截断策略，避免敏感信息与超大报文直接写入。

### 4) 落文件 / 落库策略

- Access 日志默认落文件（`access.logger`），保证全量可追溯且低耦合。
- 关键接口可按路径策略追加落库（建议表：`sys_api_access_log`），用于后台检索与统计。
- 审计日志默认落库，写入失败不得影响主业务流程（降级为错误日志）。
- 错误日志落文件并按级别分流（`WARN` 业务异常，`ERROR` 系统异常）。

### 5) 留存与归档策略

- 文件日志采用按天+大小滚动；默认保留 15 天（环境可配置扩展到 30 天）。
- 数据库日志按时间分区或按月归档；超过留存窗口执行定时清理。
- 清理任务必须可观测（记录清理数量、耗时、失败原因），并支持安全回滚。