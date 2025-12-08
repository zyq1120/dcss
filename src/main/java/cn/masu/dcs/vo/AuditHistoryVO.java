package cn.masu.dcs.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审核历史VO
 *
 * @author zyq
 * @since 2025-12-07
 */
@Data
public class AuditHistoryVO {

    /**
     * 审核记录ID
     */
    private Long id;

    /**
     * 审核人ID
     */
    private Long auditorId;

    /**
     * 审核人姓名
     */
    private String auditorName;

    /**
     * 审核状态
     */
    private Integer auditStatus;

    /**
     * 审核状态名称
     */
    private String auditStatusName;

    /**
     * 审核意见
     */
    private String auditComment;

    /**
     * 操作类型 (SAVE_DRAFT / COMPLETE_REVIEW / APPROVE / REJECT)
     */
    private String operationType;

    /**
     * 操作类型名称
     */
    private String operationTypeName;

    /**
     * 修改的字段数量
     */
    private Integer modifiedFieldCount;

    /**
     * 修改详情
     */
    private String modificationDetail;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}

