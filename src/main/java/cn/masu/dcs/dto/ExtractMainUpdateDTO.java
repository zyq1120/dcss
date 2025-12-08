package cn.masu.dcs.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

/**
 * 提取主表更新DTO
 * @author System
 */
@Data
public class ExtractMainUpdateDTO {

    @NotNull(message = "ID不能为空")
    private Long id;

    private String extractResult;
    private Double confidence;
    private Integer status;
}

