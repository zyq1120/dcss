package cn.masu.dcs.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 提取明细VO
 *
 * @author zyq
 * @since 2025-12-06
 */
@Data
public class ExtractDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 文件ID
     */
    private Long fileId;

    /**
     * 主表ID
     */
    private Long mainId;

    /**
     * 行索引
     */
    private Integer rowIndex;

    /**
     * 行数据JSON
     */
    private String rowDataJson;

    /**
     * 字段名
     */
    private String fieldName;

    /**
     * 字段值
     */
    private String fieldValue;

    /**
     * 置信度
     */
    private Double confidence;

    /**
     * 是否已校对
     */
    private Integer isVerified;
}

