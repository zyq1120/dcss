package cn.masu.dcs.vo;

import lombok.Data;
import java.util.Date;

/**
 * 任务日志VO
 * @author System
 */
@Data
public class TaskLogVO {
    private Long id;
    private String taskName;
    private String taskType;
    private String taskTypeName;
    private Long targetId;
    private Integer status;
    private String statusName;
    private String errorMessage;
    private Date startTime;
    private Date endTime;
    private Long duration;
    private String durationStr;
    private Date createTime;
}

