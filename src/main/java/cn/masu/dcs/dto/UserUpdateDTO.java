package cn.masu.dcs.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

/**
 * 用户更新DTO
 * @author System
 */
@Data
public class UserUpdateDTO {

    @NotNull(message = "用户ID不能为空")
    private Long id;

    private String nickname;

    private String email;

    private String phone;

    private String avatar;

    private Integer status;
}

