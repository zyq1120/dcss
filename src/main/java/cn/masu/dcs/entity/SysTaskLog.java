package cn.masu.dcs.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 系统任务流水日志
 * @TableName sys_task_log
 */
@TableName(value ="sys_task_log")
@Data
public class SysTaskLog {
    /**
     * 主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 链路追踪ID
     */
    @TableField(value = "trace_id")
    private String traceId;

    /**
     * 关联文件ID
     */
    @TableField(value = "file_id")
    private Long fileId;

    /**
     * UPLOAD, OCR, NLP, RULE
     */
    @TableField(value = "stage")
    private String stage;

    /**
     * SUCCESS, FAIL
     */
    @TableField(value = "status")
    private String status;

    /**
     * 耗时(毫秒)
     */
    @TableField(value = "cost_ms")
    private Long costMs;

    /**
     * 
     */
    @TableField(value = "error_msg")
    private String errorMsg;

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
        SysTaskLog other = (SysTaskLog) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getTraceId() == null ? other.getTraceId() == null : this.getTraceId().equals(other.getTraceId()))
            && (this.getFileId() == null ? other.getFileId() == null : this.getFileId().equals(other.getFileId()))
            && (this.getStage() == null ? other.getStage() == null : this.getStage().equals(other.getStage()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getCostMs() == null ? other.getCostMs() == null : this.getCostMs().equals(other.getCostMs()))
            && (this.getErrorMsg() == null ? other.getErrorMsg() == null : this.getErrorMsg().equals(other.getErrorMsg()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getTraceId() == null) ? 0 : getTraceId().hashCode());
        result = prime * result + ((getFileId() == null) ? 0 : getFileId().hashCode());
        result = prime * result + ((getStage() == null) ? 0 : getStage().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getCostMs() == null) ? 0 : getCostMs().hashCode());
        result = prime * result + ((getErrorMsg() == null) ? 0 : getErrorMsg().hashCode());
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
        sb.append(", traceId=").append(traceId);
        sb.append(", fileId=").append(fileId);
        sb.append(", stage=").append(stage);
        sb.append(", status=").append(status);
        sb.append(", costMs=").append(costMs);
        sb.append(", errorMsg=").append(errorMsg);
        sb.append(", createTime=").append(createTime);
        sb.append("]");
        return sb.toString();
    }
}