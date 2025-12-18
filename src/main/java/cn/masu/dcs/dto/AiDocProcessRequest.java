package cn.masu.dcs.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

/**
 * AI文档处理请求DTO
 * <p>
 * 用于 /api/v1/ai/process 接口
 * 同时支持驼峰命名（前端）和下划线命名（Python服务）
 * </p>
 *
 * @author zyq
 * @since 2025-12-06
 */
@Data
public class AiDocProcessRequest {

    @JsonProperty("file_id")
    @JsonAlias({"fileId", "file_id"})
    private Long fileId;

    @JsonProperty("file_content")
    @JsonAlias({"fileContent", "file_content"})
    private String fileContent;

    @JsonProperty("file_name")
    @JsonAlias({"fileName", "file_name"})
    private String fileName;

    private String text;

    private AiProcessOptions options;

    @JsonProperty("template_config")
    @JsonAlias({"templateConfig", "template_config"})
    private Map<String, Object> templateConfig;

    @JsonProperty("request_id")
    @JsonAlias({"requestId", "request_id"})
    private String requestId;
}


