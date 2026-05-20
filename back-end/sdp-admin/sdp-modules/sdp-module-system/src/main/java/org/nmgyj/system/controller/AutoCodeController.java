package org.nmgyj.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.nmgyj.common.ApiResponse;
import org.nmgyj.system.dto.AutoCodeGenerateRequest;
import org.nmgyj.system.dto.AutoCodeGenerateResult;
import org.nmgyj.system.service.AutoCodeAdminService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * 代码生成入口（支持建表并生成 CRUD）。
 *
 * @author nmgyj
 * @since 2026-05-15
 */
@RestController
@RequestMapping("/api/admin/codegen")
@Tag(name = "代码生成")
public class AutoCodeController {

    private final AutoCodeAdminService autoCodeAdminService;

    public AutoCodeController(AutoCodeAdminService autoCodeAdminService) {
        this.autoCodeAdminService = autoCodeAdminService;
    }

    /**
     * 一键建表并生成 CRUD 代码（controller/service/mapper/entity）。
     *
     * @param body 请求
     * @return 生成结果
     */
    @Operation(summary = "建表并生成 CRUD")
    @PostMapping("/create-table-and-generate")
    @SaCheckPermission("system:menu:edit")
    public ApiResponse<AutoCodeGenerateResult> createTableAndGenerate(@RequestBody AutoCodeGenerateRequest body) {
        try {
            return ApiResponse.success(autoCodeAdminService.createTableAndGenerate(body));
        } catch (IllegalArgumentException ex) {
            return ApiResponse.fail(ex.getMessage());
        }
    }

    /**
     * 下载前端页面生成 ZIP 包。
     *
     * @param token 生成时返回的下载令牌
     * @return ZIP 文件流
     */
    @Operation(summary = "下载前端页面 ZIP")
    @GetMapping("/frontend-packages/{token}")
    @SaCheckPermission("system:menu:edit")
    public ResponseEntity<FileSystemResource> downloadFrontendPackage(@PathVariable("token") String token) {
        AutoCodeAdminService.FrontendZipInfo info = autoCodeAdminService.getFrontendZip(token);
        FileSystemResource resource = new FileSystemResource(Objects.requireNonNull(info.zipPath(), "ZIP 路径不能为空"));
        String filename = new String(info.filename().getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
        long contentLength;
        try {
            contentLength = resource.contentLength();
        } catch (IOException ex) {
            throw new IllegalStateException("读取 ZIP 文件失败", ex);
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_OCTET_STREAM))
                .contentLength(contentLength)
                .body(resource);
    }
}
