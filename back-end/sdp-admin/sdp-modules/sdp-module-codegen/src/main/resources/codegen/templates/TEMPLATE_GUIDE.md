# Codegen 模板维护说明

本目录用于统一维护代码生成模板，禁止在 Java 代码中继续新增大段字符串模板。

## 目录结构

- `backend/`
  - `entity.java.th.txt`
  - `mapper.java.th.txt`
  - `service.java.th.txt`
  - `controller.java.th.txt`
- `frontend/`
  - `api.ts.th.txt`
  - `view.vue.th.txt`
  - `README.md.th.txt`
- `patches/`
  - `router.snippet.ts.th.txt`
  - `menu.dynamicRouteMap.snippet.ts.th.txt`
  - `menu.treeNode.snippet.ts.th.txt`

## 模板命名规范

- 文件名采用“输出文件名 + `.th.txt`”约定。
- 生成引擎前缀为 `codegen/templates/`，后缀为 `.th.txt`。
- 模板路径与 `CodegenFacade` 中 `render(templatePath, model)` 的路径一致。

## 关键变量约定

- 公共变量
  - `pageName`：实体/页面名（PascalCase）
  - `moduleDir`：前端目录段（如 `business`）
  - `tableApiPath`：后端 API 尾路径（如 `biz_customer`）
  - `fields`：字段列表
- 后端字段变量（`BackendField`）
  - `fieldName`、`fieldNameUpper`
  - `javaType`
  - `upperSnakeColumn`
  - `commentEscaped`
  - `primaryKey`
- 前端字段变量（`FrontendField`）
  - `prop`、`label`
  - `tsType`
  - `uiType`（`text`/`number`/`decimal`/`date`/`datetime`）
  - `defaultValue`
  - `primaryKey`

## 输出路径映射

- 后端 Java 文件固定输出到 `sdp-module-business/src/main/java` 下对应包目录。
- 前端文件输出到 ZIP 包内：
  - `src/api/generated/*.ts`
  - `src/views/<moduleDir>/*.vue`
  - `patches/*.snippet.ts`
  - `README.md`

## 变更建议

- 优先改模板文件，不要改渲染器实现。
- 新增模板变量时，先在 `CodegenFacade` 统一补充 model，再更新模板。
- 变更后至少运行 `CodegenFacadeTest` 以验证核心生成路径。
