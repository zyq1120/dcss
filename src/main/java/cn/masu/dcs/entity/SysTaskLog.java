package cn.masu.dcs.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * 系统任务日志实体
 * @author System
 */
@Data
@TableName("sys_task_log")
public class SysTaskLog {

    @TableId
    private Long id;

    @TableField("task_name")
    private String taskName;

    @TableField("task_type")
    private String taskType;

    @TableField("target_id")
    private Long targetId;

    @TableField("status")
    private Integer status;

    @TableField("error_message")
    private String errorMessage;

    @TableField("start_time")
    private Date startTime;

    @TableField("end_time")
    private Date endTime;

    @TableField("duration")
    private Long duration;

    @TableField("create_time")
    private Date createTime;
}

