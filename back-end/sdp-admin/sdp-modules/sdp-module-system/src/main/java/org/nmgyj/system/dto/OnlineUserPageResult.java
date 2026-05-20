package org.nmgyj.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 在线用户分页结果。
 *
 * @author nmgyj
 * @since 2026-05-10
 */
@Schema(description = "在线用户分页")
public class OnlineUserPageResult {

    @Schema(description = "当前页数据")
    private List<OnlineUserVO> records;

    @Schema(description = "总条数")
    private long total;

    @Schema(description = "当前页码（从 1 开始）")
    private long current;

    @Schema(description = "每页条数")
    private long size;

    /**
     * @return 记录列表
     */
    public List<OnlineUserVO> getRecords() {
        return records;
    }

    /**
     * @param records 记录列表
     */
    public void setRecords(List<OnlineUserVO> records) {
        this.records = records;
    }

    /**
     * @return 总数
     */
    public long getTotal() {
        return total;
    }

    /**
     * @param total 总数
     */
    public void setTotal(long total) {
        this.total = total;
    }

    /**
     * @return 当前页
     */
    public long getCurrent() {
        return current;
    }

    /**
     * @param current 当前页
     */
    public void setCurrent(long current) {
        this.current = current;
    }

    /**
     * @return 页大小
     */
    public long getSize() {
        return size;
    }

    /**
     * @param size 页大小
     */
    public void setSize(long size) {
        this.size = size;
    }
}
