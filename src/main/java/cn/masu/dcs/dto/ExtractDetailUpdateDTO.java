package cn.masu.dcs.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

/**
 * 提取明细更新DTO
 * @author System
 */
@Data
public class ExtractDetailUpdateDTO {

    @NotNull(message = "ID不能为空")
    private Long id;

    private String fieldValue;
    private Double confidence;
    private Integer isVerified;
}

