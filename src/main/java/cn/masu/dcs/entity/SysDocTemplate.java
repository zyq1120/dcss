package cn.masu.dcs.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 文档模板实体
 * @author System
 * &#064;TableName  sys_doc_template
 */
@Data
@TableName(value = "sys_doc_template")
public class SysDocTemplate {

    /**
     * 模板ID（主键，雪花算法生成）
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 模板名称（如：成绩单模板、请假条模板）
     */
    @TableField(value = "template_name")
    private String templateName;

    /**
     * 模板编码（如：TRANSCRIPT, LEAVE_APP, ASSET_LIST）
     */
    @TableField(value = "template_code")
    private String templateCode;

    /**
     * KV字段提取配置（JSON格式，定义需要提取的键值对字段）
     */
    @TableField(value = "target_kv_config")
    private String targetKvConfig;

    /**
     * 表格字段提取配置（JSON格式，定义需要提取的表格结构）
     */
    @TableField(value = "target_table_config")
    private String targetTableConfig;

    /**
     * 校验规则配置（JSON格式，定义字段校验规则：格式/逻辑/范围）
     */
    @TableField(value = "rule_config")
    private String ruleConfig;

    /**
     * 状态：1=启用，0=禁用
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private Date updateTime;
}

