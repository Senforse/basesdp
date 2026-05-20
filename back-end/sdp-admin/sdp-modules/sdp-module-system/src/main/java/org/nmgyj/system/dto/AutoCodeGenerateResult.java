package org.nmgyj.system.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 代码生成结果。
 *
 * @author nmgyj
 * @since 2026-05-15
 */
public class AutoCodeGenerateResult {

    private String createTableSql;
    private String entityClassName;
    private String mapperClassName;
    private String serviceClassName;
    private String controllerClassName;
    private List<String> generatedFiles = new ArrayList<>();
    private List<String> frontendGeneratedFiles = new ArrayList<>();
    private String frontendZipToken;
    private String frontendZipDownloadUrl;

    public String getCreateTableSql() {
        return createTableSql;
    }

    public void setCreateTableSql(String createTableSql) {
        this.createTableSql = createTableSql;
    }

    public String getEntityClassName() {
        return entityClassName;
    }

    public void setEntityClassName(String entityClassName) {
        this.entityClassName = entityClassName;
    }

    public String getMapperClassName() {
        return mapperClassName;
    }

    public void setMapperClassName(String mapperClassName) {
        this.mapperClassName = mapperClassName;
    }

    public String getServiceClassName() {
        return serviceClassName;
    }

    public void setServiceClassName(String serviceClassName) {
        this.serviceClassName = serviceClassName;
    }

    public String getControllerClassName() {
        return controllerClassName;
    }

    public void setControllerClassName(String controllerClassName) {
        this.controllerClassName = controllerClassName;
    }

    public List<String> getGeneratedFiles() {
        return generatedFiles;
    }

    public void setGeneratedFiles(List<String> generatedFiles) {
        this.generatedFiles = generatedFiles;
    }

    public List<String> getFrontendGeneratedFiles() {
        return frontendGeneratedFiles;
    }

    public void setFrontendGeneratedFiles(List<String> frontendGeneratedFiles) {
        this.frontendGeneratedFiles = frontendGeneratedFiles;
    }

    public String getFrontendZipToken() {
        return frontendZipToken;
    }

    public void setFrontendZipToken(String frontendZipToken) {
        this.frontendZipToken = frontendZipToken;
    }

    public String getFrontendZipDownloadUrl() {
        return frontendZipDownloadUrl;
    }

    public void setFrontendZipDownloadUrl(String frontendZipDownloadUrl) {
        this.frontendZipDownloadUrl = frontendZipDownloadUrl;
    }
}
