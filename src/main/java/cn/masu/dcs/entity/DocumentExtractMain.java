package cn.masu.dcs.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文档提取主表
 * <p>
 * 存储AI提取的主要信息（KV键值对数据）
 * </p>
 *
 * @author zyq
 * @since 2025-12-06
 */
@Data
@TableName("document_extract_main")
public class DocumentExtractMain implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 文件ID
     */
    @TableField("file_id")
    private Long fileId;

    /**
     * 模板ID
     */
    @TableField("template_id")
    private Long templateId;

    /**
     * 所有者ID（学号/工号等）
     */
    @TableField("owner_id")
    private String ownerId;

    /**
     * 所有者姓名
     */
    @TableField("owner_name")
    private String ownerName;

    /**
     * KV数据JSON
     */
    @TableField("kv_data_json")
    private String kvDataJson;

    /**
     * 提取结果（完整JSON）
     */
    @TableField("extract_result")
    private String extractResult;

    /**
     * 置信度
     */
    @TableField("confidence")
    private Double confidence;

    /**
     * 状态（0-待校对，1-已校对，2-已确认）
     */
    @TableField("status")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;
}

