package cn.masu.dcs.vo;

import lombok.Data;
import java.util.Date;

/**
 * 文件详情VO
 * @author System
 */
@Data
public class FileDetailVO {
    private Long id;
    private String fileName;
    private String filePath;
    private String fileType;
    private Long fileSize;
    private Long uploadUserId;
    private String uploadUserName;
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
}

