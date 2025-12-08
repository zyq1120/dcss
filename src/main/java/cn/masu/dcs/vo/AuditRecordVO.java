package cn.masu.dcs.vo;

import lombok.Data;
import java.util.Date;

/**
 * 审核记录VO
 * @author System
 */
@Data
public class AuditRecordVO {
    private Long id;
    private Long fileId;
    private String fileName;
    private Long extractMainId;
    private Long auditorId;
    private String auditorName;
    private Integer auditStatus;
    private String auditStatusName;
    private String auditComment;
    private Date createTime;
}

