package cn.masu.dcs.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * AI负样本/训练数据表
 * @TableName ai_training_sample
 */
@TableName(value ="ai_training_sample")
@Data
public class AiTrainingSample {
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
     * 切片图路径 (MinIO)
     */
    @TableField(value = "image_region_path")
    private String imageRegionPath;

    /**
     * 坐标 [x1,y1,x2,y2]
     */
    @TableField(value = "coordinate")
    private Object coordinate;

    /**
     * 字段Key: score, course_name
     */
    @TableField(value = "field_key")
    private String fieldKey;

    /**
     * 原始错误识别值
     */
    @TableField(value = "ocr_value")
    private String ocrValue;

    /**
     * 人工修正后的真值
     */
    @TableField(value = "corrected_value")
    private String correctedValue;

    /**
     * 0:未训练 1:已加入训练集
     */
    @TableField(value = "is_trained")
    private Integer isTrained;

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
        AiTrainingSample other = (AiTrainingSample) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getFileId() == null ? other.getFileId() == null : this.getFileId().equals(other.getFileId()))
            && (this.getImageRegionPath() == null ? other.getImageRegionPath() == null : this.getImageRegionPath().equals(other.getImageRegionPath()))
            && (this.getCoordinate() == null ? other.getCoordinate() == null : this.getCoordinate().equals(other.getCoordinate()))
            && (this.getFieldKey() == null ? other.getFieldKey() == null : this.getFieldKey().equals(other.getFieldKey()))
            && (this.getOcrValue() == null ? other.getOcrValue() == null : this.getOcrValue().equals(other.getOcrValue()))
            && (this.getCorrectedValue() == null ? other.getCorrectedValue() == null : this.getCorrectedValue().equals(other.getCorrectedValue()))
            && (this.getIsTrained() == null ? other.getIsTrained() == null : this.getIsTrained().equals(other.getIsTrained()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getFileId() == null) ? 0 : getFileId().hashCode());
        result = prime * result + ((getImageRegionPath() == null) ? 0 : getImageRegionPath().hashCode());
        result = prime * result + ((getCoordinate() == null) ? 0 : getCoordinate().hashCode());
        result = prime * result + ((getFieldKey() == null) ? 0 : getFieldKey().hashCode());
        result = prime * result + ((getOcrValue() == null) ? 0 : getOcrValue().hashCode());
        result = prime * result + ((getCorrectedValue() == null) ? 0 : getCorrectedValue().hashCode());
        result = prime * result + ((getIsTrained() == null) ? 0 : getIsTrained().hashCode());
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
        sb.append(", imageRegionPath=").append(imageRegionPath);
        sb.append(", coordinate=").append(coordinate);
        sb.append(", fieldKey=").append(fieldKey);
        sb.append(", ocrValue=").append(ocrValue);
        sb.append(", correctedValue=").append(correctedValue);
        sb.append(", isTrained=").append(isTrained);
        sb.append(", createTime=").append(createTime);
        sb.append("]");
        return sb.toString();
    }
}
