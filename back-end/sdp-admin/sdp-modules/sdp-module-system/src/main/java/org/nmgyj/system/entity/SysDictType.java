package org.nmgyj.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 数据字典类型实体，对应表 sys_dict_type。
 *
 * @author nmgyj
 * @since 2026-05-22
 */
@Schema(description = "数据字典类型")
@TableName("sys_dict_type")
public class SysDictType {

    @Schema(description = "主键", example = "1")
    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    @Schema(description = "字典类型编码（唯一标识）", example = "user_status")
    @TableField("DICT_CODE")
    private String dictCode;

    @Schema(description = "字典类型名称", example = "用户状态")
    @TableField("DICT_NAME")
    private String dictName;

    @Schema(description = "描述说明", example = "用户状态字典")
    @TableField("DESCRIPTION")
    private String description;

    @Schema(description = "状态：1 正常 0 停用", example = "1")
    @TableField("STATUS")
    private Integer status;

    @Schema(description = "排序值", example = "1")
    @TableField("SORT_ORDER")
    private Integer sortOrder;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDictCode() {
        return dictCode;
    }

    public void setDictCode(String dictCode) {
        this.dictCode = dictCode;
    }

    public String getDictName() {
        return dictName;
    }

    public void setDictName(String dictName) {
        this.dictName = dictName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}