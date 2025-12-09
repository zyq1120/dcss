package cn.masu.dcs.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件列表VO
 *
 * @author zyq
 * @since 2025-12-07
 */
@Data
public class FileListVO {

    /**
     * 文件ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件大小（格式化）
     */
    private String fileSizeFormatted;

    /**
     * 处理状态
     */
    private Integer processStatus;

    /**
     * 处理状态名称
     */
    private String processStatusName;

    /**
     * 整体置信度
     */
    private Double confidence;

    /**
     * 批次号
     */
    private String batchNo;

    /**
     * 业务编号
     */
    private String fileNo;

    /**
     * 上传用户名
     */
    private String uploaderName;

    /**
     * 失败原因
     */
    private String failReason;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}

