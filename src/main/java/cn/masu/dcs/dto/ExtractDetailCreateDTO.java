package cn.masu.dcs.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 提取明细创建DTO
 * @author System
 */
@Data
public class ExtractDetailCreateDTO {

    @NotNull(message = "主表ID不能为空")
    private Long mainId;

    @NotBlank(message = "字段名不能为空")
    private String fieldName;

    @NotBlank(message = "字段值不能为空")
    private String fieldValue;

    private String fieldType;
    private Double confidence;
}

