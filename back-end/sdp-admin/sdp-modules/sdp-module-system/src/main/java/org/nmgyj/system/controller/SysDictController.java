package org.nmgyj.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.nmgyj.common.ApiResponse;
import org.nmgyj.system.entity.SysDictItem;
import org.nmgyj.system.entity.SysDictType;
import org.nmgyj.system.service.SysDictService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 数据字典管理控制器。
 *
 * @author nmgyj
 * @since 2026-05-22
 */
@RestController
@RequestMapping("/api/admin/dict")
@Tag(name = "数据字典管理")
public class SysDictController {

    private final SysDictService sysDictService;

    public SysDictController(SysDictService sysDictService) {
        this.sysDictService = sysDictService;
    }

    // ==================== 字典类型 CRUD ====================

    /**
     * 查询所有字典类型。
     *
     * @return 字典类型列表
     */
    @Operation(summary = "查询所有字典类型")
    @GetMapping("/types")
    @SaCheckPermission("system:dict:list")
    public ApiResponse<List<SysDictType>> listDictTypes() {
        return ApiResponse.success(sysDictService.listDictTypes());
    }

    /**
     * 查询启用的字典类型。
     *
     * @return 启用的字典类型列表
     */
    @Operation(summary = "查询启用的字典类型")
    @GetMapping("/types/enabled")
    public ApiResponse<List<SysDictType>> listEnabledDictTypes() {
        return ApiResponse.success(sysDictService.listEnabledDictTypes());
    }

    /**
     * 根据ID查询字典类型。
     *
     * @param id 字典类型ID
     * @return 字典类型实体
     */
    @Operation(summary = "根据ID查询字典类型")
    @GetMapping("/types/{id}")
    @SaCheckPermission("system:dict:list")
    public ApiResponse<SysDictType> getDictTypeById(@PathVariable("id") Long id) {
        SysDictType dictType = sysDictService.getDictTypeById(id);
        if (dictType == null) {
            return ApiResponse.fail("字典类型不存在");
        }
        return ApiResponse.success(dictType);
    }

    /**
     * 根据编码查询字典类型。
     *
     * @param dictCode 字典编码
     * @return 字典类型实体
     */
    @Operation(summary = "根据编码查询字典类型")
    @GetMapping("/types/code/{dictCode}")
    public ApiResponse<SysDictType> getDictTypeByCode(@PathVariable("dictCode") String dictCode) {
        SysDictType dictType = sysDictService.getDictTypeByCode(dictCode);
        if (dictType == null) {
            return ApiResponse.fail("字典类型不存在");
        }
        return ApiResponse.success(dictType);
    }

    /**
     * 新增字典类型。
     *
     * @param body 字典类型实体
     * @return 操作结果
     */
    @Operation(summary = "新增字典类型")
    @PostMapping("/types")
    @SaCheckPermission("system:dict:edit")
    public ApiResponse<Void> createDictType(@RequestBody SysDictType body) {
        try {
            sysDictService.saveDictType(body);
            return ApiResponse.success(null);
        } catch (IllegalArgumentException ex) {
            return ApiResponse.fail(ex.getMessage());
        }
    }

    /**
     * 更新字典类型。
     *
     * @param id   字典类型ID
     * @param body 字典类型实体
     * @return 操作结果
     */
    @Operation(summary = "更新字典类型")
    @PutMapping("/types/{id}")
    @SaCheckPermission("system:dict:edit")
    public ApiResponse<Void> updateDictType(@PathVariable("id") Long id, @RequestBody SysDictType body) {
        try {
            body.setId(id);
            sysDictService.updateDictType(body);
            return ApiResponse.success(null);
        } catch (IllegalArgumentException ex) {
            return ApiResponse.fail(ex.getMessage());
        }
    }

    /**
     * 删除字典类型。
     *
     * @param id 字典类型ID
     * @return 操作结果
     */
    @Operation(summary = "删除字典类型")
    @DeleteMapping("/types/{id}")
    @SaCheckPermission("system:dict:edit")
    public ApiResponse<Void> deleteDictType(@PathVariable("id") Long id) {
        try {
            sysDictService.deleteDictType(id);
            return ApiResponse.success(null);
        } catch (IllegalArgumentException ex) {
            return ApiResponse.fail(ex.getMessage());
        }
    }

    // ==================== 字典项 CRUD ====================

    /**
     * 根据字典类型ID查询字典项列表。
     *
     * @param dictTypeId 字典类型ID
     * @return 字典项列表
     */
    @Operation(summary = "根据字典类型ID查询字典项")
    @GetMapping("/items")
    @SaCheckPermission("system:dict:list")
    public ApiResponse<List<SysDictItem>> listDictItems(@RequestParam("dictTypeId") Long dictTypeId) {
        return ApiResponse.success(sysDictService.listDictItemsByTypeId(dictTypeId));
    }

    /**
     * 根据字典编码查询启用的字典项（公开接口，无需权限）。
     *
     * @param dictCode 字典编码
     * @return 字典项列表
     */
    @Operation(summary = "根据字典编码查询启用的字典项")
    @GetMapping("/items/by-code/{dictCode}")
    public ApiResponse<List<SysDictItem>> listDictItemsByCode(@PathVariable("dictCode") String dictCode) {
        return ApiResponse.success(sysDictService.listEnabledDictItemsByCode(dictCode));
    }

    /**
     * 根据ID查询字典项。
     *
     * @param id 字典项ID
     * @return 字典项实体
     */
    @Operation(summary = "根据ID查询字典项")
    @GetMapping("/items/{id}")
    @SaCheckPermission("system:dict:list")
    public ApiResponse<SysDictItem> getDictItemById(@PathVariable("id") Long id) {
        SysDictItem dictItem = sysDictService.getDictItemById(id);
        if (dictItem == null) {
            return ApiResponse.fail("字典项不存在");
        }
        return ApiResponse.success(dictItem);
    }

    /**
     * 新增字典项。
     *
     * @param body 字典项实体
     * @return 操作结果
     */
    @Operation(summary = "新增字典项")
    @PostMapping("/items")
    @SaCheckPermission("system:dict:edit")
    public ApiResponse<Void> createDictItem(@RequestBody SysDictItem body) {
        try {
            sysDictService.saveDictItem(body);
            return ApiResponse.success(null);
        } catch (IllegalArgumentException ex) {
            return ApiResponse.fail(ex.getMessage());
        }
    }

    /**
     * 更新字典项。
     *
     * @param id   字典项ID
     * @param body 字典项实体
     * @return 操作结果
     */
    @Operation(summary = "更新字典项")
    @PutMapping("/items/{id}")
    @SaCheckPermission("system:dict:edit")
    public ApiResponse<Void> updateDictItem(@PathVariable("id") Long id, @RequestBody SysDictItem body) {
        try {
            body.setId(id);
            sysDictService.updateDictItem(body);
            return ApiResponse.success(null);
        } catch (IllegalArgumentException ex) {
            return ApiResponse.fail(ex.getMessage());
        }
    }

    /**
     * 删除字典项。
     *
     * @param id 字典项ID
     * @return 操作结果
     */
    @Operation(summary = "删除字典项")
    @DeleteMapping("/items/{id}")
    @SaCheckPermission("system:dict:edit")
    public ApiResponse<Void> deleteDictItem(@PathVariable("id") Long id) {
        sysDictService.deleteDictItem(id);
        return ApiResponse.success(null);
    }
}