package cn.masu.dcs.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

/**
 * 模板更新DTO
 * @author System
 */
@Data
public class TemplateUpdateDTO {

    @NotNull(message = "模板ID不能为空")
    private Long id;

    private String templateName;

    private String templateCode;

    private String targetKvConfig;

    private String targetTableConfig;

    private String ruleConfig;

    private Integer status;
}

