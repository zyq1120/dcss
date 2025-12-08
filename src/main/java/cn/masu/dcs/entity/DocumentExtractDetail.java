package cn.masu.dcs.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文档提取明细表
 * <p>
 * 存储AI提取的表格行数据
 * </p>
 *
 * @author zyq
 * @since 2025-12-06
 */
@Data
@TableName("document_extract_detail")
public class DocumentExtractDetail implements Serializable {

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
     * 主表ID
     */
    @TableField("main_id")
    private Long mainId;

    /**
     * 行索引
     */
    @TableField("row_index")
    private Integer rowIndex;

    /**
     * 行数据JSON
     */
    @TableField("row_data_json")
    private String rowDataJson;

    /**
     * 字段名
     */
    @TableField("field_name")
    private String fieldName;

    /**
     * 字段值
     */
    @TableField("field_value")
    private String fieldValue;

    /**
     * 行置信度
     */
    @TableField("row_confidence")
    private Double confidence;

    /**
     * 是否已校对（0-未校对，1-已校对）
     */
    @TableField("is_verified")
    private Integer isVerified;

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

