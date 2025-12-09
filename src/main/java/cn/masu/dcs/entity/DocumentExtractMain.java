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
     * 文档类型（如：transcript, certificate, leave_application等）
     */
    @TableField("document_type")
    private String documentType;

    /**
     * 文档访问URL（MinIO预签名URL）
     */
    @TableField("document_url")
    private String documentUrl;

    /**
     * OCR识别的原始文本内容
     */
    @TableField("raw_text")
    private String rawText;

    /**
     * 基本信息JSON（姓名、学号、学校等）
     */
    @TableField("basic_info_json")
    private String basicInfoJson;

    /**
     * 学业信息JSON（学位、入学日期等）
     */
    @TableField("academic_info_json")
    private String academicInfoJson;

    /**
     * 证书信息JSON（证书编号、颁发日期等）
     */
    @TableField("certificate_info_json")
    private String certificateInfoJson;

    /**
     * 财务信息JSON（银行账户、贷款金额等）
     */
    @TableField("financial_info_json")
    private String financialInfoJson;

    /**
     * 请假信息JSON（请假原因、天数等）
     */
    @TableField("leave_info_json")
    private String leaveInfoJson;

    /**
     * 摘要信息
     */
    @TableField("summary")
    private String summary;

    /**
     * 表格数据JSON（课程成绩等表格）
     */
    @TableField("tables_json")
    private String tablesJson;

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

