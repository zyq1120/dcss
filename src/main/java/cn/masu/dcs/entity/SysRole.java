package cn.masu.dcs.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * 角色实体
 * @author System
 */
@Data
@TableName("sys_role")
public class SysRole {

    @TableId
    private Long id;

    @TableField("role_name")
    private String roleName;

    @TableField("role_key")
    private String roleKey;

    @TableField("status")
    private Integer status;

    @TableField("deleted")
    private Integer deleted;

    @TableField("create_time")
    private Date createTime;
}

