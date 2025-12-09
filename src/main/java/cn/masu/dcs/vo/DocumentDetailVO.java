package cn.masu.dcs.vo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * 文档详细信息VO
 *
 * @author zyq
 * @since 2025-12-09
 */
@Data
public class DocumentDetailVO {

    /**
     * 文件ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long fileId;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件URL
     */
    private String fileUrl;

    /**
     * MinIO bucket
     */
    private String bucket;

    /**
     * MinIO object
     */
    private String object;

    /**
     * 文档类型
     */
    private String documentType;

    /**
     * 整体置信度
     */
    private Double confidenceOverall;

    /**
     * 基本信息
     */
    private Map<String, Object> basicInfo;

    /**
     * 学术信息
     */
    private Map<String, Object> academicInfo;

    /**
     * 证书信息
     */
    private Map<String, Object> certificateInfo;

    /**
     * 财务信息
     */
    private Map<String, Object> financialInfo;

    /**
     * 请假信息
     */
    private Map<String, Object> leaveInfo;

    /**
     * 课程列表
     */
    private Object courses;

    /**
     * 表格数据
     */
    private Object tables;

    /**
     * 摘要
     */
    private String summary;

    /**
     * 原始文本
     */
    private String text;

    /**
     * 文档类型候选
     */
    private Map<String, Object> documentTypeCandidates;

    /**
     * 字段提取详情
     */
    private Map<String, Object> fields;

    /**
     * 上传时间
     */
    private Date uploadTime;

    /**
     * 处理状态
     */
    private Integer processStatus;

    /**
     * 处理状态名称
     */
    private String processStatusName;

    /**
     * 完整的AI结果（原始JSON）
     */
    private JsonNode aiResultRaw;
}

