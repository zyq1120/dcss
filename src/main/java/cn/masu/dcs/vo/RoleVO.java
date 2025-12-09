package cn.masu.dcs.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import java.util.Date;

/**
 * 角色VO
 * @author System
 */
@Data
public class RoleVO {
    /**
     * 角色ID - 使用ToStringSerializer避免JavaScript精度丢失
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String roleName;
    private String roleKey;
    private Integer status;
    private Date createTime;
    private Date updateTime;
}

