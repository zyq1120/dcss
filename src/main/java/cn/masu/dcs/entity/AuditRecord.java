package cn.masu.dcs.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * 审核记录实体
 * @author System
 */
@Data
@TableName("audit_record")
public class AuditRecord {

    @TableId
    private Long id;

    @TableField("file_id")
    private Long fileId;

    @TableField("extract_main_id")
    private Long extractMainId;

    @TableField("auditor_id")
    private Long auditorId;

    @TableField("audit_status")
    private Integer auditStatus;

    @TableField("audit_comment")
    private String auditComment;

    @TableField("create_time")
    private Date createTime;
}

