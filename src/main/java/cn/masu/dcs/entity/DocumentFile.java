package cn.masu.dcs.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 教务材料文件记录表
 * @TableName document_file
 */
@TableName(value ="document_file")
@Data
public class DocumentFile {
    /**
     * 主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 上传者
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 识别模版ID (可由AI自动分类填充)
     */
    @TableField(value = "template_id")
    private Long templateId;

    /**
     * 批次号
     */
    @TableField(value = "batch_no")
    private String batchNo;

    /**
     * 业务流水号 DOC20250521001
     */
    @TableField(value = "file_no")
    private String fileNo;

    /**
     * 
     */
    @TableField(value = "file_name")
    private String fileName;

    /**
     * img/pdf
     */
    @TableField(value = "file_type")
    private String fileType;

    /**
     * 
     */
    @TableField(value = "file_size")
    private Long fileSize;

    /**
     * 
     */
    @TableField(value = "minio_bucket")
    private String minioBucket;

    /**
     * 
     */
    @TableField(value = "minio_object")
    private String minioObject;

    /**
     * 缩略图访问地址
     */
    @TableField(value = "thumbnail_url")
    private String thumbnailUrl;

    /**
     * 
     */
    @TableField(value = "process_status")
    private Integer processStatus;

    /**
     * STANDARD(Paddle+BERT) / LLM_FALLBACK(大模型)
     */
    @TableField(value = "process_mode")
    private String processMode;

    /**
     * 已重试次数
     */
    @TableField(value = "retry_count")
    private Integer retryCount;

    /**
     * 失败原因
     */
    @TableField(value = "fail_reason")
    private String failReason;

    /**
     * 
     */
    @TableField(value = "deleted")
    private Integer deleted;

    /**
     * 
     */
    @TableField(value = "version")
    private Integer version;

    /**
     * 
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 
     */
    @TableField(value = "update_time")
    private Date updateTime;

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
        DocumentFile other = (DocumentFile) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getTemplateId() == null ? other.getTemplateId() == null : this.getTemplateId().equals(other.getTemplateId()))
            && (this.getBatchNo() == null ? other.getBatchNo() == null : this.getBatchNo().equals(other.getBatchNo()))
            && (this.getFileNo() == null ? other.getFileNo() == null : this.getFileNo().equals(other.getFileNo()))
            && (this.getFileName() == null ? other.getFileName() == null : this.getFileName().equals(other.getFileName()))
            && (this.getFileType() == null ? other.getFileType() == null : this.getFileType().equals(other.getFileType()))
            && (this.getFileSize() == null ? other.getFileSize() == null : this.getFileSize().equals(other.getFileSize()))
            && (this.getMinioBucket() == null ? other.getMinioBucket() == null : this.getMinioBucket().equals(other.getMinioBucket()))
            && (this.getMinioObject() == null ? other.getMinioObject() == null : this.getMinioObject().equals(other.getMinioObject()))
            && (this.getThumbnailUrl() == null ? other.getThumbnailUrl() == null : this.getThumbnailUrl().equals(other.getThumbnailUrl()))
            && (this.getProcessStatus() == null ? other.getProcessStatus() == null : this.getProcessStatus().equals(other.getProcessStatus()))
            && (this.getProcessMode() == null ? other.getProcessMode() == null : this.getProcessMode().equals(other.getProcessMode()))
            && (this.getRetryCount() == null ? other.getRetryCount() == null : this.getRetryCount().equals(other.getRetryCount()))
            && (this.getFailReason() == null ? other.getFailReason() == null : this.getFailReason().equals(other.getFailReason()))
            && (this.getDeleted() == null ? other.getDeleted() == null : this.getDeleted().equals(other.getDeleted()))
            && (this.getVersion() == null ? other.getVersion() == null : this.getVersion().equals(other.getVersion()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getTemplateId() == null) ? 0 : getTemplateId().hashCode());
        result = prime * result + ((getBatchNo() == null) ? 0 : getBatchNo().hashCode());
        result = prime * result + ((getFileNo() == null) ? 0 : getFileNo().hashCode());
        result = prime * result + ((getFileName() == null) ? 0 : getFileName().hashCode());
        result = prime * result + ((getFileType() == null) ? 0 : getFileType().hashCode());
        result = prime * result + ((getFileSize() == null) ? 0 : getFileSize().hashCode());
        result = prime * result + ((getMinioBucket() == null) ? 0 : getMinioBucket().hashCode());
        result = prime * result + ((getMinioObject() == null) ? 0 : getMinioObject().hashCode());
        result = prime * result + ((getThumbnailUrl() == null) ? 0 : getThumbnailUrl().hashCode());
        result = prime * result + ((getProcessStatus() == null) ? 0 : getProcessStatus().hashCode());
        result = prime * result + ((getProcessMode() == null) ? 0 : getProcessMode().hashCode());
        result = prime * result + ((getRetryCount() == null) ? 0 : getRetryCount().hashCode());
        result = prime * result + ((getFailReason() == null) ? 0 : getFailReason().hashCode());
        result = prime * result + ((getDeleted() == null) ? 0 : getDeleted().hashCode());
        result = prime * result + ((getVersion() == null) ? 0 : getVersion().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", userId=").append(userId);
        sb.append(", templateId=").append(templateId);
        sb.append(", batchNo=").append(batchNo);
        sb.append(", fileNo=").append(fileNo);
        sb.append(", fileName=").append(fileName);
        sb.append(", fileType=").append(fileType);
        sb.append(", fileSize=").append(fileSize);
        sb.append(", minioBucket=").append(minioBucket);
        sb.append(", minioObject=").append(minioObject);
        sb.append(", thumbnailUrl=").append(thumbnailUrl);
        sb.append(", processStatus=").append(processStatus);
        sb.append(", processMode=").append(processMode);
        sb.append(", retryCount=").append(retryCount);
        sb.append(", failReason=").append(failReason);
        sb.append(", deleted=").append(deleted);
        sb.append(", version=").append(version);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append("]");
        return sb.toString();
    }
}