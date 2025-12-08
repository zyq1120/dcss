package cn.masu.dcs.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

/**
 * 审核提交DTO
 */
@Data
public class AuditSubmitDTO {

    @NotNull(message = "文件ID不能为空")
    private Long fileId;

    @NotNull(message = "审核状态不能为空")
    private Integer auditStatus;

    private String auditComment;
}
