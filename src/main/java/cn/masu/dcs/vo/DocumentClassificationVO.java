package cn.masu.dcs.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.Date;

/**
 * 文档分类列表VO
 *
 * @author zyq
 * @since 2025-12-09
 */
@Data
public class DocumentClassificationVO {

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
     * 文档类型
     */
    private String documentType;

    /**
     * 整体置信度
     */
    private Double confidence;

    /**
     * 摘要信息
     */
    private String summary;

    /**
     * 姓名（从basic_info提取）
     */
    private String name;

    /**
     * 学号/证件号
     */
    private String idNumber;

    /**
     * 学校名称
     */
    private String schoolName;

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
     * 文件URL（预览用）
     */
    private String fileUrl;
}

