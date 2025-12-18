package cn.masu.dcs.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import java.util.Date;

/**
 * 文件详情VO
 * @author System
 */
@Data
public class FileDetailVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String fileName;
    private String filePath;
    private String fileType;
    private Long fileSize;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long uploadUserId;

    private String uploadUserName;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long templateId;

    private String templateName;
    private Integer status;
    private String statusName;
    private Integer ocrStatus;
    private String ocrStatusName;
    private Integer nlpStatus;
    private String nlpStatusName;
    private Integer auditStatus;
    private String auditStatusName;
    private Date createTime;
    private Date updateTime;

    /**
     * 获取文件ID（别名，方便前端获取）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    public Long getFileId() {
        return id;
    }
}

