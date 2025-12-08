package cn.masu.dcs.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * 模板创建DTO
 * @author System
 */
@Data
public class TemplateCreateDTO {
    @NotBlank(message = "模板编码不能为空")
    private String templateCode;

    @NotBlank(message = "模板名称不能为空")
    private String templateName;

    private String targetKvConfig;

    private String targetTableConfig;

    private String ruleConfig;

    private Integer status = 1;
}

