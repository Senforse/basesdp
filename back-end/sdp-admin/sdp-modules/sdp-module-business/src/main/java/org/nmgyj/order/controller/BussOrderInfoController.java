package org.nmgyj.order.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.nmgyj.common.ApiResponse;
import org.nmgyj.order.entity.BussOrderInfo;
import org.nmgyj.order.service.BussOrderInfoService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * BussOrderInfo 管理接口。
 *
 * @author nmgyj
 * @since 2026-05-15
 */
@RestController
@RequestMapping("/api/admin/buss_order_info")
@Tag(name = "BussOrderInfo")
public class BussOrderInfoController {

    private final BussOrderInfoService bussOrderInfoService;

    public BussOrderInfoController(BussOrderInfoService bussOrderInfoService) {
        this.bussOrderInfoService = bussOrderInfoService;
    }

    @Operation(summary = "列表")
    @GetMapping
    @SaCheckPermission("business:buss_order_info:list")
    public ApiResponse<List<BussOrderInfo>> list() {
        return ApiResponse.success(bussOrderInfoService.listAll());
    }

    @Operation(summary = "详情")
    @GetMapping("/{id}")
    @SaCheckPermission("business:buss_order_info:list")
    public ApiResponse<BussOrderInfo> detail(@PathVariable("id") Long id) {
        return ApiResponse.success(bussOrderInfoService.getById(id));
    }

    @Operation(summary = "新增")
    @PostMapping
    @SaCheckPermission("business:buss_order_info:add")
    public ApiResponse<Void> create(@RequestBody BussOrderInfo body) {
        try {
            bussOrderInfoService.save(body);
            return ApiResponse.success(null);
        } catch (IllegalArgumentException ex) {
            return ApiResponse.fail(ex.getMessage());
        }
    }

    @Operation(summary = "更新")
    @PutMapping("/{id}")
    @SaCheckPermission("business:buss_order_info:update")
    public ApiResponse<Void> update(@PathVariable("id") Long id, @RequestBody BussOrderInfo body) {
        try {
            body.setId(id);
            bussOrderInfoService.update(body);
            return ApiResponse.success(null);
        } catch (IllegalArgumentException ex) {
            return ApiResponse.fail(ex.getMessage());
        }
    }

    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    @SaCheckPermission("business:buss_order_info:delete")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        bussOrderInfoService.delete(id);
        return ApiResponse.success(null);
    }
}
