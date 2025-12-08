package cn.masu.dcs.vo;

import lombok.Data;
import java.util.Date;
import java.util.List;

/**
 * 用户VO
 * @author System
 */
@Data
public class UserVO {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private String avatar;
    private Integer status;
    private Date createTime;
    private Date updateTime;
    private List<String> roles;
    private List<String> roleNames;
    private List<Long> roleIds;
}

