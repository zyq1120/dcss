package cn.masu.dcs.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * 文档文件实体
 * @author zyq
 */
@Data
@TableName("document_file")
public class DocumentFile {

    @TableId
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("template_id")
    private Long templateId;

    @TableField("batch_no")
    private String batchNo;

    @TableField("file_no")
    private String fileNo;

    @TableField("file_name")
    private String fileName;

    @TableField("file_type")
    private String fileType;

    @TableField("file_size")
    private Long fileSize;

    @TableField("minio_bucket")
    private String minioBucket;

    @TableField("minio_object")
    private String minioObject;

    @TableField("thumbnail_url")
    private String thumbnailUrl;

    @TableField("process_status")
    private Integer processStatus;

    @TableField("process_mode")
    private String processMode;

    @TableField("retry_count")
    private Integer retryCount;

    @TableField("fail_reason")
    private String failReason;

    @TableField("deleted")
    private Integer deleted;

    @TableField("version")
    private Integer version;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;
}

