package org.nmgyj.codegen.service;

import org.nmgyj.codegen.model.AutoCodeColumn;
import org.nmgyj.codegen.model.AutoCodeResult;
import org.nmgyj.codegen.model.AutoCodeTable;
import org.nmgyj.codegen.model.FrontendGenerateResult;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class CodegenFacade {

    private final TemplateRenderer templateRenderer;

    public CodegenFacade(@Qualifier("thymeleafTemplateRenderer") TemplateRenderer templateRenderer) {
        this.templateRenderer = templateRenderer;
    }

    public AutoCodeResult generateCrud(AutoCodeTable table) {
        return generateCrud(table, resolveBusinessJavaRoot());
    }

    public AutoCodeResult generateCrud(AutoCodeTable table, Path businessJavaRoot) {
        validateTable(table);
        String modulePkg = normalizePackage(table.getModulePackage());
        String rootPackage = "org.nmgyj." + modulePkg;
        String entityName = toPascalCase(table.getTableName());
        String mapperName = entityName + "Mapper";
        String serviceName = entityName + "Service";
        String controllerName = entityName + "Controller";

        List<BackendField> backendFields = buildBackendFields(table);

        Path entityPath = businessJavaRoot.resolve(packageToDir(rootPackage + ".entity")).resolve(entityName + ".java");
        Path mapperPath = businessJavaRoot.resolve(packageToDir(rootPackage + ".mapper")).resolve(mapperName + ".java");
        Path servicePath = businessJavaRoot.resolve(packageToDir(rootPackage + ".service")).resolve(serviceName + ".java");
        Path controllerPath = businessJavaRoot.resolve(packageToDir(rootPackage + ".controller")).resolve(controllerName + ".java");

        Map<String, Object> entityModel = new LinkedHashMap<>();
        entityModel.put("rootPackage", rootPackage);
        entityModel.put("tableName", table.getTableName());
        entityModel.put("tableComment", tableComment(table));
        entityModel.put("entityName", entityName);
        entityModel.put("imports", buildEntityImports(table));
        entityModel.put("fields", backendFields);
        writeJavaFile(entityPath, templateRenderer.render("backend/entity.java", entityModel));

        Map<String, Object> mapperModel = new LinkedHashMap<>();
        mapperModel.put("rootPackage", rootPackage);
        mapperModel.put("entityName", entityName);
        mapperModel.put("mapperName", mapperName);
        writeJavaFile(mapperPath, templateRenderer.render("backend/mapper.java", mapperModel));

        Map<String, Object> serviceModel = new LinkedHashMap<>();
        serviceModel.put("rootPackage", rootPackage);
        serviceModel.put("entityName", entityName);
        serviceModel.put("mapperName", mapperName);
        serviceModel.put("mapperVar", lowerFirst(mapperName));
        serviceModel.put("serviceName", serviceName);
        serviceModel.put("pkField", resolvePrimaryKeyField(table));
        serviceModel.put("pkFieldUpper", upperFirst(resolvePrimaryKeyField(table)));
        serviceModel.put("pkType", resolvePrimaryKeyType(table).getSimpleName());
        writeJavaFile(servicePath, templateRenderer.render("backend/service.java", serviceModel));

        Map<String, Object> controllerModel = new LinkedHashMap<>();
        controllerModel.put("rootPackage", rootPackage);
        controllerModel.put("entityName", entityName);
        controllerModel.put("serviceName", serviceName);
        controllerModel.put("serviceVar", lowerFirst(serviceName));
        controllerModel.put("controllerName", controllerName);
        controllerModel.put("basePath", table.getTableName().toLowerCase(Locale.ROOT));
        controllerModel.put("permPrefix", "business:" + table.getTableName().toLowerCase(Locale.ROOT));
        controllerModel.put("pkType", resolvePrimaryKeyType(table).getSimpleName());
        controllerModel.put("pkFieldUpper", upperFirst(resolvePrimaryKeyField(table)));
        writeJavaFile(controllerPath, templateRenderer.render("backend/controller.java", controllerModel));

        AutoCodeResult result = new AutoCodeResult();
        result.setEntityClassName(entityName);
        result.setMapperClassName(mapperName);
        result.setServiceClassName(serviceName);
        result.setControllerClassName(controllerName);
        result.getGeneratedFiles().add(entityPath.toString());
        result.getGeneratedFiles().add(mapperPath.toString());
        result.getGeneratedFiles().add(servicePath.toString());
        result.getGeneratedFiles().add(controllerPath.toString());
        return result;
    }

    public FrontendGenerateResult renderFrontendFiles(AutoCodeTable table, AutoCodeResult coreResult, String createTableSql) {
        String moduleDir = normalizeModuleDir(table.getModulePackage());
        String pageName = coreResult.getEntityClassName();
        String varName = lowerFirst(pageName);
        String tableApiPath = table.getTableName().toLowerCase(Locale.ROOT);
        String routePath = "/" + moduleDir + "/" + table.getTableName().toLowerCase(Locale.ROOT).replace("_", "-");
        List<FrontendField> fields = buildFrontendFields(table);

        Map<String, Object> frontendModel = new LinkedHashMap<>();
        frontendModel.put("pageName", pageName);
        frontendModel.put("varName", varName);
        frontendModel.put("tableApiPath", tableApiPath);
        frontendModel.put("moduleDir", moduleDir);
        frontendModel.put("routePath", routePath);
        frontendModel.put("fields", fields);
        frontendModel.put("dynamicRouteKey", pageName);
        frontendModel.put("permPrefix", "business:" + table.getTableName().toLowerCase(Locale.ROOT));
        frontendModel.put("apiImportPath", "@/api/generated/" + varName);
        frontendModel.put("createTableSql", createTableSql);

        String apiPath = "src/api/generated/" + varName + ".ts";
        String viewPath = "src/views/" + moduleDir + "/" + pageName + ".vue";
        String readmePath = "README.md";

        Map<String, String> files = new LinkedHashMap<>();
        files.put(apiPath, templateRenderer.render("frontend/api.ts", frontendModel));
        files.put(viewPath, templateRenderer.render("frontend/view.vue", frontendModel));
        files.put(readmePath, templateRenderer.render("frontend/README.md", frontendModel));
        return new FrontendGenerateResult(files);
    }

    private List<String> buildEntityImports(AutoCodeTable table) {
        Set<String> imports = new LinkedHashSet<>();
        imports.add("com.baomidou.mybatisplus.annotation.IdType");
        imports.add("com.baomidou.mybatisplus.annotation.TableField");
        imports.add("com.baomidou.mybatisplus.annotation.TableId");
        imports.add("com.baomidou.mybatisplus.annotation.TableName");
        imports.add("io.swagger.v3.oas.annotations.media.Schema");
        for (AutoCodeColumn c : table.getColumns()) {
            Class<?> type = toJavaType(c);
            if (type.equals(LocalDateTime.class)) {
                imports.add("com.fasterxml.jackson.annotation.JsonFormat");
                imports.add("org.springframework.format.annotation.DateTimeFormat");
                imports.add("java.time.LocalDateTime");
            } else if (type.equals(LocalDate.class)) {
                imports.add("com.fasterxml.jackson.annotation.JsonFormat");
                imports.add("org.springframework.format.annotation.DateTimeFormat");
                imports.add("java.time.LocalDate");
            } else if (type.equals(Long.class)) {
                imports.add("com.fasterxml.jackson.databind.annotation.JsonSerialize");
                imports.add("com.fasterxml.jackson.databind.ser.std.ToStringSerializer");
            } else if (type.equals(BigDecimal.class)) {
                imports.add("java.math.BigDecimal");
            }
        }
        return imports.stream().sorted(Comparator.naturalOrder()).toList();
    }

    private List<BackendField> buildBackendFields(AutoCodeTable table) {
        return table.getColumns().stream().map(column -> {
            String fieldName = toCamelCase(column.getColumnName());
            String javaType = toJavaType(column).getSimpleName();
            String comment = hasText(column.getComment()) ? column.getComment() : fieldName;
            return new BackendField(
                    fieldName,
                    upperFirst(fieldName),
                    javaType,
                    "Boolean".equals(javaType) ? "is" : "get",
                    toUpperSnake(column.getColumnName()),
                    escapeForJava(comment),
                    column.isPrimaryKey(),
                    resolveIdType(javaType),
                    "Long".equals(javaType),
                    "LocalDateTime".equals(javaType),
                    "LocalDate".equals(javaType)
            );
        }).toList();
    }

    private String resolveIdType(String javaType) {
        if ("Long".equals(javaType)) {
            return "IdType.ASSIGN_ID";
        }
        if ("String".equals(javaType)) {
            return "IdType.ASSIGN_UUID";
        }
        return "IdType.INPUT";
    }

    private List<FrontendField> buildFrontendFields(AutoCodeTable table) {
        return table.getColumns().stream().map(column -> {
            String dbType = column.getDbType().toUpperCase(Locale.ROOT);
            String prop = toCamelCase(column.getColumnName());
            String label = hasText(column.getComment()) ? column.getComment() : prop;
            if (dbType.contains("BIGINT")) {
                return new FrontendField(prop, label, "string", "text", "''", column.isPrimaryKey());
            }
            if (dbType.equals("INT") || dbType.contains("INTEGER")) {
                return new FrontendField(prop, label, "number", "number", "undefined", column.isPrimaryKey());
            }
            if (dbType.contains("DECIMAL") || dbType.contains("NUMERIC") || dbType.contains("NUMBER")) {
                return new FrontendField(prop, label, "number", "decimal", "undefined", column.isPrimaryKey());
            }
            if (dbType.contains("DATETIME") || dbType.contains("TIMESTAMP")) {
                return new FrontendField(prop, label, "string", "datetime", "''", column.isPrimaryKey());
            }
            if (dbType.equals("DATE")) {
                return new FrontendField(prop, label, "string", "date", "''", column.isPrimaryKey());
            }
            return new FrontendField(prop, label, "string", "text", "''", column.isPrimaryKey());
        }).toList();
    }

    private void validateTable(AutoCodeTable table) {
        if (table == null) {
            throw new IllegalArgumentException("表定义不能为空");
        }
        if (!isValidIdentifier(table.getTableName())) {
            throw new IllegalArgumentException("表名不合法，仅支持字母、数字、下划线，且不能数字开头");
        }
        if (table.getColumns() == null || table.getColumns().isEmpty()) {
            throw new IllegalArgumentException("字段不能为空");
        }
        for (AutoCodeColumn column : table.getColumns()) {
            if (!isValidIdentifier(column.getColumnName())) {
                throw new IllegalArgumentException("字段名不合法：" + column.getColumnName());
            }
            if (!hasText(column.getDbType())) {
                throw new IllegalArgumentException("字段类型不能为空：" + column.getColumnName());
            }
        }
    }

    private Path resolveBusinessJavaRoot() {
        Path current = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        for (int i = 0; i < 12 && current != null; i++) {
            Path candidate = current.resolve("sdp-modules").resolve("sdp-module-business").resolve("src").resolve("main").resolve("java");
            if (Files.isDirectory(candidate)) {
                return candidate;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("未找到 sdp-module-business/src/main/java，请在 sdp-admin 目录内运行服务");
    }

    private void writeJavaFile(Path path, String content) {
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, content, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("写入文件失败: " + path, ex);
        }
    }

    private Class<?> resolvePrimaryKeyType(AutoCodeTable table) {
        return table.getColumns().stream().filter(AutoCodeColumn::isPrimaryKey).findFirst()
                .<Class<?>>map(this::toJavaType).orElseGet(() -> Long.class);
    }

    private String resolvePrimaryKeyField(AutoCodeTable table) {
        return table.getColumns().stream().filter(AutoCodeColumn::isPrimaryKey).findFirst()
                .map(c -> toCamelCase(c.getColumnName())).orElse("id");
    }

    private Class<?> toJavaType(AutoCodeColumn column) {
        String type = column.getDbType().toLowerCase(Locale.ROOT);
        if (type.contains("bigint")) {
            return Long.class;
        }
        if (type.contains("int")) {
            return Integer.class;
        }
        if (type.contains("decimal") || type.contains("numeric") || type.contains("number")) {
            return BigDecimal.class;
        }
        if (type.contains("date") && !type.contains("time")) {
            return LocalDate.class;
        }
        if (type.contains("time")) {
            return LocalDateTime.class;
        }
        if (type.contains("char") || type.contains("text") || type.contains("clob")) {
            return String.class;
        }
        return String.class;
    }

    private String tableComment(AutoCodeTable table) {
        if (hasText(table.getTableComment())) {
            return table.getTableComment();
        }
        return table.getTableName() + " 实体";
    }

    private String normalizePackage(String pkg) {
        if (!hasText(pkg)) {
            return "business";
        }
        return pkg.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_.]", "");
    }

    private String normalizeModuleDir(String modulePackage) {
        if (!hasText(modulePackage)) {
            return "business";
        }
        return modulePackage.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9/_-]", "");
    }

    private String packageToDir(String pkg) {
        return pkg.replace(".", "/");
    }

    private String toPascalCase(String value) {
        StringBuilder sb = new StringBuilder();
        for (String part : value.toLowerCase(Locale.ROOT).split("_")) {
            if (part.isEmpty()) {
                continue;
            }
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        if (sb.isEmpty()) {
            throw new IllegalArgumentException("无法从表名生成类名: " + value);
        }
        return sb.toString();
    }

    private String toCamelCase(String value) {
        String pascal = toPascalCase(value);
        return lowerFirst(pascal);
    }

    private String toUpperSnake(String value) {
        return value.replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase(Locale.ROOT);
    }

    private String lowerFirst(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        return Character.toLowerCase(value.charAt(0)) + value.substring(1);
    }

    private String upperFirst(String value) {
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    private boolean isValidIdentifier(String value) {
        return hasText(value) && value.matches("^[A-Za-z][A-Za-z0-9_]*$");
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String escapeForJava(String value) {
        return Objects.toString(value, "").replace("\"", "\\\"");
    }

    public record BackendField(
            String fieldName,
            String fieldNameUpper,
            String javaType,
            String getterPrefix,
            String upperSnakeColumn,
            String commentEscaped,
            boolean primaryKey,
            String idType,
            boolean longType,
            boolean localDateTime,
            boolean localDate) {
    }

    public record FrontendField(
            String prop,
            String label,
            String tsType,
            String uiType,
            String defaultValue,
            boolean primaryKey) {
    }
}
