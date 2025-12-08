package cn.masu.dcs.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * 完成校对DTO
 *
 * @author zyq
 * @since 2025-12-07
 */
@Data
public class ReviewCompleteDTO {

    /**
     * 文件ID
     */
    @NotNull(message = "文件ID不能为空")
    private Long fileId;

    /**
     * 提取主表ID
     */
    private Long extractMainId;

    /**
     * 字段数据
     */
    @NotNull(message = "字段数据不能为空")
    private Map<String, Object> fields;

    /**
     * 完成备注
     */
    private String comment;

    /**
     * 是否确认归档
     */
    private Boolean confirmArchive = true;
}

