package org.nmgyj.codegen.service;

import org.junit.jupiter.api.Test;
import org.nmgyj.codegen.model.AutoCodeColumn;
import org.nmgyj.codegen.model.AutoCodeResult;
import org.nmgyj.codegen.model.AutoCodeTable;
import org.nmgyj.codegen.model.FrontendGenerateResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodegenFacadeTest {

    @Test
    void shouldGenerateBackendAndFrontendFilesFromTemplates() throws IOException {
        CodegenFacade facade = new CodegenFacade(new ThymeleafTemplateRenderer());
        AutoCodeTable table = sampleTable();
        Path tempJavaRoot = Files.createTempDirectory("codegen-backend-test-");

        AutoCodeResult backend = facade.generateCrud(table, tempJavaRoot);
        FrontendGenerateResult frontend = facade.renderFrontendFiles(table, backend, "CREATE TABLE BIZ_CUSTOMER (...)");

        assertEquals(4, backend.getGeneratedFiles().size());
        for (String generatedFile : backend.getGeneratedFiles()) {
            assertTrue(Files.exists(Path.of(generatedFile)));
        }

        String entityFile = Files.readString(Path.of(backend.getGeneratedFiles().getFirst()));
        assertTrue(entityFile.contains("public class BizCustomer"));
        assertTrue(entityFile.contains("@TableName(\"biz_customer\")"));

        List<String> frontendFiles = frontend.getGeneratedFiles();
        assertTrue(frontendFiles.contains("src/api/generated/bizCustomer.ts"));
        assertTrue(frontendFiles.contains("src/views/business/BizCustomer.vue"));
        assertTrue(frontend.getFiles().get("README.md").contains("前端页面生成包"));
    }

    private AutoCodeTable sampleTable() {
        AutoCodeColumn id = new AutoCodeColumn();
        id.setColumnName("id");
        id.setComment("主键");
        id.setDbType("BIGINT");
        id.setPrimaryKey(true);
        id.setNullable(false);

        AutoCodeColumn name = new AutoCodeColumn();
        name.setColumnName("customer_name");
        name.setComment("客户名称");
        name.setDbType("VARCHAR");
        name.setLength(64);
        name.setNullable(false);

        AutoCodeTable table = new AutoCodeTable();
        table.setTableName("biz_customer");
        table.setTableComment("客户信息");
        table.setModulePackage("business");
        table.setColumns(List.of(id, name));
        return table;
    }
}
