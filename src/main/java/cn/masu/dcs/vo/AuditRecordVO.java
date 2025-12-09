package cn.masu.dcs.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import java.util.Date;

/**
 * 审核记录VO
 * @author System
 */
@Data
public class AuditRecordVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long fileId;

    private String fileName;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long extractMainId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long auditorId;

    private String auditorName;
    private Integer auditStatus;
    private String auditStatusName;
    private String auditComment;
    private Date createTime;
}

