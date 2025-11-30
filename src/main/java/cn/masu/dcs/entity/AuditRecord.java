package cn.masu.dcs.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 人工审核/校对记录
 * @TableName audit_record
 */
@TableName(value ="audit_record")
@Data
public class AuditRecord {
    /**
     * 主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 文件ID
     */
    @TableField(value = "file_id")
    private Long fileId;

    /**
     * 审核人ID
     */
    @TableField(value = "auditor_id")
    private Long auditorId;

    /**
     * APPROVE, REJECT, MODIFY
     */
    @TableField(value = "action_type")
    private String actionType;

    /**
     *
     */
    @TableField(value = "comment")
    private String comment;

    /**
     *
     */
    @TableField(value = "create_time")
    private Date createTime;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        AuditRecord other = (AuditRecord) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getFileId() == null ? other.getFileId() == null : this.getFileId().equals(other.getFileId()))
            && (this.getAuditorId() == null ? other.getAuditorId() == null : this.getAuditorId().equals(other.getAuditorId()))
            && (this.getActionType() == null ? other.getActionType() == null : this.getActionType().equals(other.getActionType()))
            && (this.getComment() == null ? other.getComment() == null : this.getComment().equals(other.getComment()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getFileId() == null) ? 0 : getFileId().hashCode());
        result = prime * result + ((getAuditorId() == null) ? 0 : getAuditorId().hashCode());
        result = prime * result + ((getActionType() == null) ? 0 : getActionType().hashCode());
        result = prime * result + ((getComment() == null) ? 0 : getComment().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", fileId=").append(fileId);
        sb.append(", auditorId=").append(auditorId);
        sb.append(", actionType=").append(actionType);
        sb.append(", comment=").append(comment);
        sb.append(", createTime=").append(createTime);
        sb.append("]");
        return sb.toString();
    }
}
