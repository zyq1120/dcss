package cn.masu.dcs.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 文档表格明细提取表
 * @TableName document_extract_detail
 */
@TableName(value ="document_extract_detail")
@Data
public class DocumentExtractDetail {
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
     * 关联提取主表ID
     */
    @TableField(value = "main_id")
    private Long mainId;

    /**
     * 行号
     */
    @TableField(value = "row_index")
    private Integer rowIndex;

    /**
     * 该行的结构化数据
     */
    @TableField(value = "row_data_json")
    private Object rowDataJson;

    /**
     * 0:正常,1:逻辑警告,2:格式警告
     */
    @TableField(value = "logic_flag")
    private Integer logicFlag;

    /**
     * 警告信息
     */
    @TableField(value = "logic_msg")
    private String logicMsg;

    /**
     * 该行数据的平均置信度
     */
    @TableField(value = "row_confidence")
    private BigDecimal rowConfidence;

    /**
     * 是否经过人工修改
     */
    @TableField(value = "is_corrected")
    private Integer isCorrected;

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
        DocumentExtractDetail other = (DocumentExtractDetail) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getFileId() == null ? other.getFileId() == null : this.getFileId().equals(other.getFileId()))
            && (this.getMainId() == null ? other.getMainId() == null : this.getMainId().equals(other.getMainId()))
            && (this.getRowIndex() == null ? other.getRowIndex() == null : this.getRowIndex().equals(other.getRowIndex()))
            && (this.getRowDataJson() == null ? other.getRowDataJson() == null : this.getRowDataJson().equals(other.getRowDataJson()))
            && (this.getLogicFlag() == null ? other.getLogicFlag() == null : this.getLogicFlag().equals(other.getLogicFlag()))
            && (this.getLogicMsg() == null ? other.getLogicMsg() == null : this.getLogicMsg().equals(other.getLogicMsg()))
            && (this.getRowConfidence() == null ? other.getRowConfidence() == null : this.getRowConfidence().equals(other.getRowConfidence()))
            && (this.getIsCorrected() == null ? other.getIsCorrected() == null : this.getIsCorrected().equals(other.getIsCorrected()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getFileId() == null) ? 0 : getFileId().hashCode());
        result = prime * result + ((getMainId() == null) ? 0 : getMainId().hashCode());
        result = prime * result + ((getRowIndex() == null) ? 0 : getRowIndex().hashCode());
        result = prime * result + ((getRowDataJson() == null) ? 0 : getRowDataJson().hashCode());
        result = prime * result + ((getLogicFlag() == null) ? 0 : getLogicFlag().hashCode());
        result = prime * result + ((getLogicMsg() == null) ? 0 : getLogicMsg().hashCode());
        result = prime * result + ((getRowConfidence() == null) ? 0 : getRowConfidence().hashCode());
        result = prime * result + ((getIsCorrected() == null) ? 0 : getIsCorrected().hashCode());
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
        sb.append(", mainId=").append(mainId);
        sb.append(", rowIndex=").append(rowIndex);
        sb.append(", rowDataJson=").append(rowDataJson);
        sb.append(", logicFlag=").append(logicFlag);
        sb.append(", logicMsg=").append(logicMsg);
        sb.append(", rowConfidence=").append(rowConfidence);
        sb.append(", isCorrected=").append(isCorrected);
        sb.append(", createTime=").append(createTime);
        sb.append("]");
        return sb.toString();
    }
}
