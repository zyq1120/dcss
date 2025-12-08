package cn.masu.dcs.dto;

import lombok.Data;

/**
 * 校对任务查询DTO
 *
 * @author zyq
 * @since 2025-12-07
 */
@Data
public class ReviewQueryDTO {

    /**
     * 当前页
     */
    private Integer current = 1;

    /**
     * 每页大小
     */
    private Integer size = 10;

    /**
     * 关键字（文件名）
     */
    private String keyword;

    /**
     * 置信度范围 - 最小值
     */
    private Double minConfidence;

    /**
     * 置信度范围 - 最大值
     */
    private Double maxConfidence;

    /**
     * 开始日期 yyyy-MM-dd
     */
    private String startDate;

    /**
     * 结束日期 yyyy-MM-dd
     */
    private String endDate;

    /**
     * 排序字段 (create_time / confidence)
     */
    private String orderBy = "create_time";

    /**
     * 排序方向 (asc / desc)
     */
    private String orderDirection = "desc";
}

