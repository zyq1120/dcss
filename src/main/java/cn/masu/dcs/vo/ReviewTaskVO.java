package cn.masu.dcs.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 校对任务VO
 *
 * @author zyq
 * @since 2025-12-07
 */
@Data
public class ReviewTaskVO {

    /**
     * 文件ID
     */
    private Long id;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 文件大小（格式化）
     */
    private String fileSizeFormatted;

    /**
     * 整体置信度
     */
    private Double confidence;

    /**
     * 提取主表ID
     */
    private Long extractMainId;

    /**
     * 姓名
     */
    private String ownerName;

    /**
     * 学号/证件号
     */
    private String ownerId;

    /**
     * 字段数量
     */
    private Integer fieldCount;

    /**
     * 已修改字段数量
     */
    private Integer modifiedFieldCount;

    /**
     * 是否已部分审核
     */
    private Boolean partiallyReviewed;

    /**
     * 最后修改人
     */
    private String lastModifier;

    /**
     * 最后修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastModifyTime;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 文件预览URL
     */
    private String previewUrl;
}

