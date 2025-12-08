package cn.masu.dcs.vo;

import lombok.Data;
import java.util.Date;

/**
 * 模板VO
 * @author System
 */
@Data
public class TemplateVO {
    private Long id;
    private String templateCode;
    private String templateName;
    private Object targetKvConfig;
    private Object targetTableConfig;
    private Object ruleConfig;
    private Integer status;
    private Date createTime;
    private Date updateTime;
}

