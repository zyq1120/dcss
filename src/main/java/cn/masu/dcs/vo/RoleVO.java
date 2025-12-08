package cn.masu.dcs.vo;

import lombok.Data;
import java.util.Date;

/**
 * 角色VO
 * @author System
 */
@Data
public class RoleVO {
    private Long id;
    private String roleName;
    private String roleKey;
    private Integer status;
    private Date createTime;
    private Date updateTime;
}

