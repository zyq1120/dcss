package cn.masu.dcs.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

/**
 * 文件状态更新DTO
 * @author System
 */
@Data
public class FileUpdateStatusDTO {

    @NotNull(message = "文件ID不能为空")
    private Long id;

    private Integer status;
    private Integer ocrStatus;
    private Integer nlpStatus;
    private Integer auditStatus;
}

