# 项目介绍
  这个项目是股份公司自研项目，旨在高效实现企业数值化转型，摆脱工业软件供应商的依赖

项目基于springboot框架开发，逐步构建微服务架构，进行项目的拓展

##  项目结构

sdp-admin (Root Parent)
├── sdp-common (公共套件)
│   ├── sdp-common-core (核心封装)
│   ├── sdp-common-security (安全/租户权限)
│   └── sdp-common-mybatis (数据库/多租户插件)
├── sdp-api (Feign/DTO契约)
├── sdp-modules (业务模块)
│   ├── sdp-module-system (系统/多租户管理)
│   └── sdp-module-business (你的核心业务)
└── sdp-gateway (微服务网关入口)
└── sdp-server (单体服务启动类)

### 1. 结构设计：谁该放什么？

建议采用以下模块分层，这能完美解决你担心的多租户和插件化扩展问题：

#### **A. sdp-dependencies (根父工程 / 管理者)**

- **角色**：版本仲裁者。
    
- **内容**：只有 `pom.xml`。通过 `<dependencyManagement>` 锁定所有三方库版本（Spring Boot, Cloud, Alibaba, Mybatis-Plus 等）。
    
- **原则**：**不引入具体依赖**。它只负责定义“如果有人要用这个包，必须用哪个版本”。
    

#### **B. sdp-common (核心公共模块)**

- **角色**：全平台的基石。
    
- **内容**：
    
    - **sdp-common-core**: 存放全局常量、枚举、自定义异常、BaseEntity、**租户上下文 (TenantContext)**。
        
    - **sdp-common-security**: 封装 Spring Security 配置、JWT 解析逻辑。
        
    - **sdp-common-mybatis**: 封装 Mybatis-Plus 配置、**多租户拦截器逻辑**。
        
- **原则**：高复用，无具体业务逻辑。
    

#### **C. sdp-api (接口契约模块)**

- **角色**：服务间的“外交部”。
    
- **内容**：存放各业务模块暴露给外部调用的 **FeignClient 接口** 和 **DTO**。
    
- **原则**：轻量。这样 A 模块调用 B 模块时，只需依赖 `sdp-api`，而不需要引入 B 模块沉重的业务实现。
    

#### **D. sdp-modules (业务逻辑模块)**

- **角色**：真正的业务执行者。
    
- **示例**：`sdp-module-system`（用户权限）、`sdp-module-infra`（文件/日志）。
    
- **原则**：**高内聚**。每个模块拥有自己的 Controller, Service, Mapper。
    

#### **E. sdp-server (启动入口 / 网关)**

- **角色**：平台的脸面。
    
- **内容**：`sdp-gateway`（网关）、`sdp-auth-server`（认证中心）。
    

---

### 2. 依赖引入的最佳实践：如何做到“轻量”？

为了避免项目变成“臃肿的泥球”，请遵循以下依赖引用准则：

1. **最小依赖原则**：
    
    - 在父工程 `sdp-admin` 的 `dependencies`（注意不是 Management）里只放每个模块都**百分之百**需要的包，例如：`lombok`、`slf4j`。
        
2. **按需引入**：
    
    - 数据库相关的包（如 `mybatis-plus-boot-starter`）只放在 `sdp-common-mybatis` 中。
        
    - 需要数据库支持的业务模块引入 `sdp-common-mybatis`，不需要的（如纯网关）则不引入。
        
3. **避免传递依赖污染**：
    
    - 尽量使用 `<optional>true</optional>` 或在子模块中使用 `<exclusions>` 排除掉不用的包，保持最终生成的 JAR 包瘦身。
        

---

### 3. 针对“多租户”与“动态插件”的科学预留

既然人手不足，我们要利用 **Spring Boot 3 + JDK 21** 的特性进行“低成本预留”：

- **多租户预留**： 在 `sdp-common-core` 中定义一个 `TenantContextHolder` (利用 `ThreadLocal`)。 在 `sdp-common-mybatis` 中配置 Mybatis-Plus 插件，统一从 `TenantContextHolder` 取租户 ID。 _这样，无论你以后是字段隔离还是分库隔离，业务代码都感知不到租户的存在。_
    
- **动态插件预留**： 在 `sdp-common-core` 中定义一套标准接口契约。 业务模块通过 `Spring Bean` 的自动注入来加载实现类。 _未来想做真正的动态加载时，只需在网关层做一个动态路由，或者引入 `Classloader` 隔离机制即可。_

## 接口文档

http://localhost:8080/swagger-ui.html

常见几种方式：

发链接（同一网络 / 已部署环境）
对方能访问你的服务时，直接给：
http://<你的主机>:<端口>/swagger-ui.html

发 OpenAPI 文件（最通用）
让对方或用脚本访问：
http://<主机>:<端口>/v3/api-docs
保存成 openapi.json（或 YAML，若你们另行配置了 yaml 路径）。
同事可导入：Postman、Apifox、Insomnia、Stoplight 等，即可离线看文档、生成客户端。

静态站点托管（不接你们后端也能看文档）
用上面的 JSON + 任意 Swagger UI 静态页（或 Redoc）托管到内网 Wiki、对象存储、GitHub Pages，只共享「文档」，不暴露运行中的 API。

前后端协作
前端若用 OpenAPI 生成 Mock（如 Orval、openapi-typescript），同样以 /v3/api-docs 或导出的 JSON 为单一数据源即可。

