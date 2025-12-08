package cn.masu.dcs.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Request body for /api/v1/ai/process.
 * @author zyq
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AiProcessRequest {

    @JsonProperty("file_path")
    private String filePath;

    private String text;

    @JsonProperty("file_id")
    private Long fileId;

    @NotNull(message = "template_config is required")
    @JsonProperty("template_config")
    private TemplateConfig templateConfig;

    private Options options;

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TemplateConfig {
        private List<FieldConfig> fields;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FieldConfig {
        private String name;
        private String type;
        private Boolean required;
        private List<String> patterns;
        private Map<String, Object> validation;
        private String description;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Options {
        @JsonProperty("enhance_image")
        private Boolean enhanceImage;
    }
}
