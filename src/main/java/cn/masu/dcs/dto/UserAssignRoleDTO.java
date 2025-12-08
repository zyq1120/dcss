package cn.masu.dcs.dto;

import lombok.Data;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 用户分配角色DTO
 * @author System
 */
@Data
public class UserAssignRoleDTO {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotEmpty(message = "角色ID列表不能为空")
    private List<Long> roleIds;
}

