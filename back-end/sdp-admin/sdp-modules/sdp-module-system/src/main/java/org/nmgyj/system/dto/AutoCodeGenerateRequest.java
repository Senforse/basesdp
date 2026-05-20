package org.nmgyj.system.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 代码生成请求（建表 + 生成 CRUD）。
 *
 * @author nmgyj
 * @since 2026-05-15
 */
public class AutoCodeGenerateRequest {

    private String tableName;
    private String tableComment;
    private String modulePackage = "business";
    private List<AutoCodeColumnRequest> columns = new ArrayList<>();

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTableComment() {
        return tableComment;
    }

    public void setTableComment(String tableComment) {
        this.tableComment = tableComment;
    }

    public String getModulePackage() {
        return modulePackage;
    }

    public void setModulePackage(String modulePackage) {
        this.modulePackage = modulePackage;
    }

    public List<AutoCodeColumnRequest> getColumns() {
        return columns;
    }

    public void setColumns(List<AutoCodeColumnRequest> columns) {
        this.columns = columns;
    }
}
