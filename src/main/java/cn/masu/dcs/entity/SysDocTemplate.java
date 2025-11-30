package cn.masu.dcs.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 文档识别模版配置表
 * @TableName sys_doc_template
 */
@TableName(value ="sys_doc_template")
@Data
public class SysDocTemplate {
    /**
     * 模版ID (雪花算法)
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 模版名称：学生成绩单/请假条/资产采购单
     */
    @TableField(value = "template_name")
    private String templateName;

    /**
     * 编码：TRANSCRIPT, LEAVE_APP, ASSET_LIST
     */
    @TableField(value = "template_code")
    private String templateCode;

    /**
     * KV键值对字段提取规则
     */
    @TableField(value = "target_kv_config")
    private Object targetKvConfig;

    /**
     * 表格明细提取规则
     */
    @TableField(value = "target_table_config")
    private Object targetTableConfig;

    /**
     * 校验与纠错规则配置
     */
    @TableField(value = "rule_config")
    private Object ruleConfig;

    /**
     * 1启用 0禁用
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 
     */
    @TableField(value = "update_time")
    private Date updateTime;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        SysDocTemplate other = (SysDocTemplate) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getTemplateName() == null ? other.getTemplateName() == null : this.getTemplateName().equals(other.getTemplateName()))
            && (this.getTemplateCode() == null ? other.getTemplateCode() == null : this.getTemplateCode().equals(other.getTemplateCode()))
            && (this.getTargetKvConfig() == null ? other.getTargetKvConfig() == null : this.getTargetKvConfig().equals(other.getTargetKvConfig()))
            && (this.getTargetTableConfig() == null ? other.getTargetTableConfig() == null : this.getTargetTableConfig().equals(other.getTargetTableConfig()))
            && (this.getRuleConfig() == null ? other.getRuleConfig() == null : this.getRuleConfig().equals(other.getRuleConfig()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getTemplateName() == null) ? 0 : getTemplateName().hashCode());
        result = prime * result + ((getTemplateCode() == null) ? 0 : getTemplateCode().hashCode());
        result = prime * result + ((getTargetKvConfig() == null) ? 0 : getTargetKvConfig().hashCode());
        result = prime * result + ((getTargetTableConfig() == null) ? 0 : getTargetTableConfig().hashCode());
        result = prime * result + ((getRuleConfig() == null) ? 0 : getRuleConfig().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", templateName=").append(templateName);
        sb.append(", templateCode=").append(templateCode);
        sb.append(", targetKvConfig=").append(targetKvConfig);
        sb.append(", targetTableConfig=").append(targetTableConfig);
        sb.append(", ruleConfig=").append(ruleConfig);
        sb.append(", status=").append(status);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append("]");
        return sb.toString();
    }
}