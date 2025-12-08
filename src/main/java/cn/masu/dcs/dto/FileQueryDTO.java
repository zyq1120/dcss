package cn.masu.dcs.dto;

import lombok.Data;

/**
 * 文件查询DTO
 *
 * @author zyq
 * @since 2025-12-07
 */
@Data
public class FileQueryDTO {

    /**
     * 当前页
     */
    private Integer current = 1;

    /**
     * 每页大小
     */
    private Integer size = 10;

    /**
     * 处理状态 0=pending, 1=queued, 2=processing, 3=manual, 4=archived, 5=failed
     */
    private Integer processStatus;

    /**
     * 关键字（文件名）
     */
    private String keyword;

    /**
     * 上传用户ID
     */
    private Long userId;

    /**
     * 模板ID
     */
    private Long templateId;

    /**
     * 批次号
     */
    private String batchNo;

    /**
     * 开始日期 yyyy-MM-dd
     */
    private String startDate;

    /**
     * 结束日期 yyyy-MM-dd
     */
    private String endDate;
}

