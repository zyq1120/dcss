package cn.masu.dcs.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import java.util.Date;
import java.util.List;

/**
 * 用户VO
 * @author System
 */
@Data
public class UserVO {
    /**
     * 用户ID - 使用ToStringSerializer避免JavaScript精度丢失
     */
    @JsonSerialize(using = ToStringSerializer.class)
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

    /**
     * 角色ID列表 - 使用ToStringSerializer避免JavaScript精度丢失
     */
    @JsonSerialize(contentUsing = ToStringSerializer.class)
    private List<Long> roleIds;
}

