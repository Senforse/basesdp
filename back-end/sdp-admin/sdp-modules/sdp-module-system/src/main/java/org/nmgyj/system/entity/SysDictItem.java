package org.nmgyj.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 数据字典项实体，对应表 sys_dict_item。
 *
 * @author nmgyj
 * @since 2026-05-22
 */
@Schema(description = "数据字典项")
@TableName("sys_dict_item")
public class SysDictItem {

    @Schema(description = "主键", example = "1")
    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    @Schema(description = "所属字典类型ID", example = "1")
    @TableField("DICT_TYPE_ID")
    private Long dictTypeId;

    @Schema(description = "字典项值", example = "1")
    @TableField("ITEM_VALUE")
    private String itemValue;

    @Schema(description = "字典项标签", example = "启用")
    @TableField("ITEM_LABEL")
    private String itemLabel;

    @Schema(description = "描述说明", example = "用户状态为启用")
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

    public Long getDictTypeId() {
        return dictTypeId;
    }

    public void setDictTypeId(Long dictTypeId) {
        this.dictTypeId = dictTypeId;
    }

    public String getItemValue() {
        return itemValue;
    }

    public void setItemValue(String itemValue) {
        this.itemValue = itemValue;
    }

    public String getItemLabel() {
        return itemLabel;
    }

    public void setItemLabel(String itemLabel) {
        this.itemLabel = itemLabel;
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