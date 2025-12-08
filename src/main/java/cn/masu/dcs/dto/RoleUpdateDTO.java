package cn.masu.dcs.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

/**
 * 角色更新DTO
 * @author System
 */
@Data
public class RoleUpdateDTO {

    @NotNull(message = "角色ID不能为空")
    private Long id;

    private String roleName;

    private String roleKey;

    private Integer status;
}

