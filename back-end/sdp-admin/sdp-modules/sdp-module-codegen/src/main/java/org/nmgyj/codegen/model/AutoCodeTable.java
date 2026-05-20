package org.nmgyj.codegen.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 自动代码生成的表定义。
 */
public class AutoCodeTable {

    private String tableName;
    private String tableComment;
    private String modulePackage = "business";
    private List<AutoCodeColumn> columns = new ArrayList<>();

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

    public List<AutoCodeColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<AutoCodeColumn> columns) {
        this.columns = columns;
    }
}
