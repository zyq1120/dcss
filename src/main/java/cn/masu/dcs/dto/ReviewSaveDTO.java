package cn.masu.dcs.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * 保存校对草稿DTO
 *
 * @author zyq
 * @since 2025-12-07
 */
@Data
public class ReviewSaveDTO {

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
     * key: 字段名
     * value: 字段值
     */
    @NotNull(message = "字段数据不能为空")
    private Map<String, Object> fields;

    /**
     * 备注
     */
    private String comment;
}

