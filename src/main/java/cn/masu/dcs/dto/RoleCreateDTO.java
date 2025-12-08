package cn.masu.dcs.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * 角色创建DTO
 * @author System
 */
@Data
public class RoleCreateDTO {

    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    @NotBlank(message = "角色标识不能为空")
    private String roleKey;

    private Integer status = 1;
}

