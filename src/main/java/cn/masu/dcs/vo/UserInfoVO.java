package cn.masu.dcs.vo;

import lombok.Data;
import java.util.List;

/**
 * 用户信息VO
 * @author System
 */
@Data
public class UserInfoVO {
    private Long userId;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private String avatar;
    private List<String> roles;
    private List<String> permissions;
}

