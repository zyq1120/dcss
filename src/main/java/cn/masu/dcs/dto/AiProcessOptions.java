package cn.masu.dcs.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 处理选项 DTO。
 */
@Data
public class AiProcessOptions {

    @JsonProperty("enhance_image")
    private Boolean enhanceImage;

    @JsonProperty("handwriting_mode")
    private Boolean handwritingMode;

    @JsonProperty("llm_only")
    private Boolean llmOnly;

    @JsonProperty("disable_ai")
    private Boolean disableAi;

    @JsonProperty("use_llm")
    private Boolean useLlm;

    @JsonProperty("llm_image")
    private Boolean llmImage;

    @JsonProperty("auto_infer_fields")
    private Boolean autoInferFields;

    @JsonProperty("analyze_ocr")
    private Boolean analyzeOcr;

    @JsonProperty("detect_table")
    private Boolean detectTable;

    @JsonProperty("table_force")
    private Boolean tableForce;

    @JsonProperty("table_detect_threshold")
    private Double tableDetectThreshold;

    @JsonProperty("llm_provider")
    private String llmProvider;

    @JsonProperty("llm_model")
    private String llmModel;
}

