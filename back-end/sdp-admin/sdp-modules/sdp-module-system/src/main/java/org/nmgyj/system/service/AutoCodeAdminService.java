package org.nmgyj.system.service;

import org.nmgyj.codegen.model.AutoCodeColumn;
import org.nmgyj.codegen.model.AutoCodeResult;
import org.nmgyj.codegen.model.AutoCodeTable;
import org.nmgyj.codegen.model.FrontendGenerateResult;
import org.nmgyj.codegen.service.CodegenFacade;
import org.nmgyj.system.dto.AutoCodeColumnRequest;
import org.nmgyj.system.dto.AutoCodeGenerateRequest;
import org.nmgyj.system.dto.AutoCodeGenerateResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 管理端代码生成服务（建表 + CRUD 文件生成）。
 *
 * @author nmgyj
 * @since 2026-05-15
 */
@Service
public class AutoCodeAdminService {

    private final JdbcTemplate jdbcTemplate;
    private final CodegenFacade codegenFacade;
    private final Map<String, FrontendZipInfo> frontendZipStore = new ConcurrentHashMap<>();

    public AutoCodeAdminService(JdbcTemplate jdbcTemplate, CodegenFacade codegenFacade) {
        this.jdbcTemplate = jdbcTemplate;
        this.codegenFacade = codegenFacade;
    }

    @Transactional(rollbackFor = Exception.class)
    public AutoCodeGenerateResult createTableAndGenerate(AutoCodeGenerateRequest request) {
        validateRequest(request);
        String createTableSql = Objects.requireNonNull(buildCreateTableSql(request), "建表 SQL 不能为空");
        jdbcTemplate.execute(createTableSql);

        AutoCodeTable table = new AutoCodeTable();
        table.setTableName(request.getTableName());
        table.setTableComment(request.getTableComment());
        table.setModulePackage(request.getModulePackage());
        table.setColumns(request.getColumns().stream().map(this::toCoreColumn).collect(Collectors.toList()));

        AutoCodeResult coreResult = codegenFacade.generateCrud(table);
        AutoCodeGenerateResult out = new AutoCodeGenerateResult();
        out.setCreateTableSql(createTableSql);
        out.setEntityClassName(coreResult.getEntityClassName());
        out.setMapperClassName(coreResult.getMapperClassName());
        out.setServiceClassName(coreResult.getServiceClassName());
        out.setControllerClassName(coreResult.getControllerClassName());
        out.setGeneratedFiles(coreResult.getGeneratedFiles());

        FrontendZipInfo frontendZipInfo = generateFrontendZip(table, coreResult, createTableSql);
        out.setFrontendGeneratedFiles(frontendZipInfo.generatedFiles());
        out.setFrontendZipToken(frontendZipInfo.token());
        out.setFrontendZipDownloadUrl("/api/admin/codegen/frontend-packages/" + frontendZipInfo.token());
        return out;
    }

    public FrontendZipInfo getFrontendZip(String token) {
        FrontendZipInfo info = frontendZipStore.get(token);
        if (info == null) {
            throw new IllegalArgumentException("前端页面包不存在或已过期，请重新生成");
        }
        return info;
    }

    private void validateRequest(AutoCodeGenerateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("请求不能为空");
        }
        if (!isValidName(request.getTableName())) {
            throw new IllegalArgumentException("表名不合法，仅支持字母、数字、下划线，且不能数字开头");
        }
        if (request.getColumns() == null || request.getColumns().isEmpty()) {
            throw new IllegalArgumentException("请至少填写一个字段");
        }
        long pkCount = request.getColumns().stream().filter(c -> Boolean.TRUE.equals(c.getPrimaryKey())).count();
        if (pkCount == 0) {
            throw new IllegalArgumentException("请至少指定一个主键字段");
        }
        for (AutoCodeColumnRequest column : request.getColumns()) {
            if (!isValidName(column.getColumnName())) {
                throw new IllegalArgumentException("字段名不合法：" + column.getColumnName());
            }
            if (!StringUtils.hasText(column.getDbType())) {
                throw new IllegalArgumentException("字段类型不能为空：" + column.getColumnName());
            }
        }
    }

    private String buildCreateTableSql(AutoCodeGenerateRequest request) {
        List<String> lines = new ArrayList<>();
        List<String> pkColumns = new ArrayList<>();
        for (AutoCodeColumnRequest column : request.getColumns()) {
            String colName = column.getColumnName().toUpperCase(Locale.ROOT);
            String dataType = normalizeDbType(column);
            StringBuilder line = new StringBuilder();
            line.append(colName).append(" ").append(dataType);
            if (Boolean.FALSE.equals(column.getNullable())) {
                line.append(" NOT NULL");
            }
            lines.add(line.toString());
            if (Boolean.TRUE.equals(column.getPrimaryKey())) {
                pkColumns.add(colName);
            }
        }
        lines.add("PRIMARY KEY (" + String.join(", ", pkColumns) + ")");
        return "CREATE TABLE " + request.getTableName().toUpperCase(Locale.ROOT) + " (\n    "
                + String.join(",\n    ", lines) + "\n)";
    }

    private String normalizeDbType(AutoCodeColumnRequest column) {
        String dbType = column.getDbType().toUpperCase(Locale.ROOT).trim();
        if ("VARCHAR".equals(dbType) || "VARCHAR2".equals(dbType)) {
            int len = column.getLength() == null || column.getLength() <= 0 ? 64 : column.getLength();
            return "VARCHAR(" + len + ")";
        }
        if ("CHAR".equals(dbType)) {
            int len = column.getLength() == null || column.getLength() <= 0 ? 1 : column.getLength();
            return "CHAR(" + len + ")";
        }
        if ("DECIMAL".equals(dbType) || "NUMERIC".equals(dbType) || "NUMBER".equals(dbType)) {
            int precision = column.getLength() == null || column.getLength() <= 0 ? 18 : column.getLength();
            int scale = column.getScale() == null || column.getScale() < 0 ? 2 : column.getScale();
            return "DECIMAL(" + precision + "," + scale + ")";
        }
        if ("DATETIME".equals(dbType)) {
            return "TIMESTAMP";
        }
        if ("TEXT".equals(dbType)) {
            return "CLOB";
        }
        return dbType;
    }

    private AutoCodeColumn toCoreColumn(AutoCodeColumnRequest source) {
        AutoCodeColumn c = new AutoCodeColumn();
        c.setColumnName(source.getColumnName());
        c.setComment(source.getComment());
        c.setDbType(source.getDbType());
        c.setLength(source.getLength());
        c.setScale(source.getScale());
        c.setNullable(source.getNullable() == null || source.getNullable());
        c.setPrimaryKey(Boolean.TRUE.equals(source.getPrimaryKey()));
        return c;
    }

    private boolean isValidName(String value) {
        return StringUtils.hasText(value) && value.matches("^[A-Za-z][A-Za-z0-9_]*$");
    }

    private FrontendZipInfo generateFrontendZip(
            AutoCodeTable table,
            AutoCodeResult coreResult,
            String createTableSql) {
        FrontendGenerateResult frontendGenerateResult = codegenFacade.renderFrontendFiles(table, coreResult, createTableSql);
        Map<String, String> files = frontendGenerateResult.getFiles();

        String token = UUID.randomUUID().toString().replace("-", "");
        String zipName = "frontend-page-" + table.getTableName().toLowerCase(Locale.ROOT) + "-" + token.substring(0, 8) + ".zip";
        Path zipPath = writeZipFile(token, zipName, files);
        FrontendZipInfo info = new FrontendZipInfo(token, zipName, zipPath, frontendGenerateResult.getGeneratedFiles());
        frontendZipStore.put(token, info);
        return info;
    }

    private Path writeZipFile(String token, String zipName, Map<String, String> files) {
        try {
            Path baseDir = Files.createTempDirectory("sdp-codegen-frontend-");
            Path zipPath = baseDir.resolve(zipName);
            try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath), StandardCharsets.UTF_8)) {
                for (Map.Entry<String, String> entry : files.entrySet()) {
                    zos.putNextEntry(new ZipEntry(entry.getKey().replace("\\", "/")));
                    zos.write(entry.getValue().getBytes(StandardCharsets.UTF_8));
                    zos.closeEntry();
                }
            }
            return zipPath;
        } catch (IOException ex) {
            throw new IllegalStateException("生成前端 ZIP 失败，token=" + token, ex);
        }
    }

    public record FrontendZipInfo(String token, String filename, Path zipPath, List<String> generatedFiles) {
    }
}
