package org.nmgyj.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 系统用户实体，对应表 {@code sys_user}；达梦 Oracle 兼容模式下列名为大写须与物理列一致。
 *
 * @author nmgyj
 * @since 2026-05-10
 */
@Schema(description = "系统用户")
@TableName("sys_user")
public class SysUser {

    @Schema(description = "主键", example = "1")
    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    @Schema(description = "登录用户名", example = "zhangsan")
    @TableField("USERNAME")
    private String username;

    @Schema(description = "密码（BCrypt 或明文，创建/修改时传入）", example = "$2a$10$...")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @TableField("PASSWORD")
    private String password;

    @Schema(description = "展示名称", example = "张三")
    @TableField("DISPLAY_NAME")
    private String displayName;

    @Schema(description = "状态：1 启用 0 停用", example = "1")
    @TableField("STATUS")
    private Integer status;

    /**
     * 所属组织 ID。
     */
    @Schema(description = "所属组织主键", example = "100")
    @TableField("DEPT_ID")
    private Long deptId;

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
     * @return 用户名
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username 用户名
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return 密码（序列化时默认不回传）
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password 密码
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return 展示名称
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @param displayName 展示名称
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
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
     * @return 组织 ID
     */
    public Long getDeptId() {
        return deptId;
    }

    /**
     * @param deptId 组织 ID
     */
    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }
}
