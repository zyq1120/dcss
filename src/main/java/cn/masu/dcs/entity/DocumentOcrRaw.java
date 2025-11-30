package cn.masu.dcs.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * AI原始识别结果表
 * @TableName document_ocr_raw
 */
@TableName(value ="document_ocr_raw")
@Data
public class DocumentOcrRaw {
    /**
     * 主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 关联文件ID
     */
    @TableField(value = "file_id")
    private Long fileId;

    /**
     * OCR识别出的所有纯文本拼接
     */
    @TableField(value = "full_text")
    private String fullText;

    /**
     * 带坐标的原始数据
     */
    @TableField(value = "raw_data_json")
    private Object rawDataJson;

    /**
     * 文本块数量
     */
    @TableField(value = "text_count")
    private Integer textCount;

    /**
     * 整页平均置信度
     */
    @TableField(value = "avg_confidence")
    private BigDecimal avgConfidence;

    /**
     * 是否使用了大模型
     */
    @TableField(value = "is_llm_used")
    private Integer isLlmUsed;

    /**
     * LLM Token消耗量
     */
    @TableField(value = "token_usage")
    private Integer tokenUsage;

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
        DocumentOcrRaw other = (DocumentOcrRaw) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getFileId() == null ? other.getFileId() == null : this.getFileId().equals(other.getFileId()))
            && (this.getFullText() == null ? other.getFullText() == null : this.getFullText().equals(other.getFullText()))
            && (this.getRawDataJson() == null ? other.getRawDataJson() == null : this.getRawDataJson().equals(other.getRawDataJson()))
            && (this.getTextCount() == null ? other.getTextCount() == null : this.getTextCount().equals(other.getTextCount()))
            && (this.getAvgConfidence() == null ? other.getAvgConfidence() == null : this.getAvgConfidence().equals(other.getAvgConfidence()))
            && (this.getIsLlmUsed() == null ? other.getIsLlmUsed() == null : this.getIsLlmUsed().equals(other.getIsLlmUsed()))
            && (this.getTokenUsage() == null ? other.getTokenUsage() == null : this.getTokenUsage().equals(other.getTokenUsage()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getFileId() == null) ? 0 : getFileId().hashCode());
        result = prime * result + ((getFullText() == null) ? 0 : getFullText().hashCode());
        result = prime * result + ((getRawDataJson() == null) ? 0 : getRawDataJson().hashCode());
        result = prime * result + ((getTextCount() == null) ? 0 : getTextCount().hashCode());
        result = prime * result + ((getAvgConfidence() == null) ? 0 : getAvgConfidence().hashCode());
        result = prime * result + ((getIsLlmUsed() == null) ? 0 : getIsLlmUsed().hashCode());
        result = prime * result + ((getTokenUsage() == null) ? 0 : getTokenUsage().hashCode());
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
        sb.append(", fullText=").append(fullText);
        sb.append(", rawDataJson=").append(rawDataJson);
        sb.append(", textCount=").append(textCount);
        sb.append(", avgConfidence=").append(avgConfidence);
        sb.append(", isLlmUsed=").append(isLlmUsed);
        sb.append(", tokenUsage=").append(tokenUsage);
        sb.append(", createTime=").append(createTime);
        sb.append("]");
        return sb.toString();
    }
}