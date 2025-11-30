package cn.masu.dcs.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 文档关键信息提取主表(KV)
 * @TableName document_extract_main
 */
@TableName(value ="document_extract_main")
@Data
public class DocumentExtractMain {
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
     * 冗余模版ID
     */
    @TableField(value = "template_id")
    private Long templateId;

    /**
     * 通用学号/工号
     */
    @TableField(value = "owner_id")
    private String ownerId;

    /**
     * 通用姓名/申请人
     */
    @TableField(value = "owner_name")
    private String ownerName;

    /**
     * 文档业务日期
     */
    @TableField(value = "doc_date")
    private Date docDate;

    /**
     * 所有非表格的提取数据
     */
    @TableField(value = "kv_data_json")
    private Object kvDataJson;

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
        DocumentExtractMain other = (DocumentExtractMain) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getFileId() == null ? other.getFileId() == null : this.getFileId().equals(other.getFileId()))
            && (this.getTemplateId() == null ? other.getTemplateId() == null : this.getTemplateId().equals(other.getTemplateId()))
            && (this.getOwnerId() == null ? other.getOwnerId() == null : this.getOwnerId().equals(other.getOwnerId()))
            && (this.getOwnerName() == null ? other.getOwnerName() == null : this.getOwnerName().equals(other.getOwnerName()))
            && (this.getDocDate() == null ? other.getDocDate() == null : this.getDocDate().equals(other.getDocDate()))
            && (this.getKvDataJson() == null ? other.getKvDataJson() == null : this.getKvDataJson().equals(other.getKvDataJson()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getFileId() == null) ? 0 : getFileId().hashCode());
        result = prime * result + ((getTemplateId() == null) ? 0 : getTemplateId().hashCode());
        result = prime * result + ((getOwnerId() == null) ? 0 : getOwnerId().hashCode());
        result = prime * result + ((getOwnerName() == null) ? 0 : getOwnerName().hashCode());
        result = prime * result + ((getDocDate() == null) ? 0 : getDocDate().hashCode());
        result = prime * result + ((getKvDataJson() == null) ? 0 : getKvDataJson().hashCode());
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
        sb.append(", fileId=").append(fileId);
        sb.append(", templateId=").append(templateId);
        sb.append(", ownerId=").append(ownerId);
        sb.append(", ownerName=").append(ownerName);
        sb.append(", docDate=").append(docDate);
        sb.append(", kvDataJson=").append(kvDataJson);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append("]");
        return sb.toString();
    }
}
