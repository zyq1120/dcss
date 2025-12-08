package cn.masu.dcs.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 提取编辑VO
 * <p>
 * 用于前端展示和编辑AI提取的数据
 * 包含主表信息和明细列表
 * </p>
 *
 * @author zyq
 * @since 2025-12-06
 */
@Data
public class ExtractEditVO implements Serializable {

    @java.io.Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主表ID
     */
    private Long mainId;

    /**
     * 文件ID
     */
    private Long fileId;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 模板ID
     */
    private Long templateId;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 状态（0-待校对，1-已校对，2-已确认）
     */
    private Integer status;

    /**
     * 明细列表
     */
    private List<ExtractDetailVO> details;
}

