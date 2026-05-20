package org.nmgyj.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

/**
 * 组织架构实体，对应 {@code sys_organization}；达梦下列名为大写。
 *
 * @author nmgyj
 * @since 2026-05-10
 */
@Schema(description = "组织架构")
@TableName("sys_organization")
public class SysOrganization {

    @Schema(description = "主键", example = "1")
    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    @Schema(description = "父组织 ID，根为 0", example = "0")
    @TableField("PARENT_ID")
    private Long parentId;

    @Schema(description = "组织编码", example = "NMGYJ_ROOT")
    @TableField("ORG_CODE")
    private String orgCode;

    @Schema(description = "组织名称", example = "总公司")
    @TableField("ORG_NAME")
    private String orgName;

    @Schema(description = "排序", example = "0")
    @TableField("SORT_ORDER")
    private Integer sortOrder;

    @Schema(description = "状态：1 正常 0 停用", example = "1")
    @TableField("STATUS")
    private Integer status;

    @Schema(description = "子组织（树接口填充）")
    @TableField(exist = false)
    private List<SysOrganization> children = new ArrayList<>();

    /**
     * @return 主键
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id 主键
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return 父组织 ID
     */
    public Long getParentId() {
        return parentId;
    }

    /**
     * @param parentId 父组织 ID
     */
    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    /**
     * @return 组织编码
     */
    public String getOrgCode() {
        return orgCode;
    }

    /**
     * @param orgCode 组织编码
     */
    public void setOrgCode(String orgCode) {
        this.orgCode = orgCode;
    }

    /**
     * @return 组织名称
     */
    public String getOrgName() {
        return orgName;
    }

    /**
     * @param orgName 组织名称
     */
    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    /**
     * @return 排序
     */
    public Integer getSortOrder() {
        return sortOrder;
    }

    /**
     * @param sortOrder 排序
     */
    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    /**
     * @return 状态
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * @param status 状态
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * @return 子组织列表
     */
    public List<SysOrganization> getChildren() {
        return children;
    }

    /**
     * @param children 子组织列表
     */
    public void setChildren(List<SysOrganization> children) {
        this.children = children != null ? children : new ArrayList<>();
    }
}
