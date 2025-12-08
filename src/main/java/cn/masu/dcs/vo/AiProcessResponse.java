package cn.masu.dcs.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * AI处理响应VO
 * <p>
 * 用于返回OCR/NLP处理结果
 * </p>
 *
 * @author zyq
 * @since 2025-12-06
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AiProcessResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文件ID
     */
    @JsonProperty("file_id")
    private Long fileId;

    /**
     * 文件名
     */
    @JsonProperty("file_name")
    private String fileName;

    /**
     * 处理是否成功
     */
    private Boolean success = true;

    /**
     * 错误消息（仅在失败时填充）
     */
    @JsonProperty("error_message")
    private String errorMessage;

    /**
     * 整体置信度（0-1之间）
     */
    private Double confidence;

    /**
     * OCR识别结果
     */
    @JsonProperty("ocr_result")
    private OcrResult ocrResult;

    /**
     * NLP提取结果
     */
    @JsonProperty("nlp_result")
    private NlpResult nlpResult;

    /**
     * 是否使用了LLM兜底
     */
    @JsonProperty("llm_used")
    private Boolean llmUsed;

    /**
     * 处理耗时（秒）
     */
    @JsonProperty("processing_time")
    private Double processingTime;

    // 兼容旧版本字段
    @Deprecated
    private OcrResult ocr;

    @Deprecated
    private FieldsResult fields;

    @Deprecated
    private List<Object> entities;

    @Deprecated
    private List<Object> relations;

    /**
     * 获取整体置信度
     * <p>
     * 如果confidence字段为空，则从ocrResult中获取
     * </p>
     *
     * @return 置信度值
     */
    public Double getConfidence() {
        if (confidence != null) {
            return confidence;
        }
        if (ocrResult != null && ocrResult.getConfidence() != null) {
            return ocrResult.getConfidence();
        }
        return 0.0;
    }

    /**
     * OCR识别结果
     */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OcrResult implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 完整文本
         */
        @JsonProperty("full_text")
        private String fullText;

        /**
         * 文本块列表
         */
        @JsonProperty("text_blocks")
        private List<TextBlock> textBlocks;

        /**
         * 版面分析结果
         */
        @JsonProperty("layout_analysis")
        private LayoutAnalysis layoutAnalysis;

        /**
         * 平均置信度
         */
        private Double confidence;

        /**
         * OCR数据源（paddleocr/tesseract/llm）
         */
        private String source;

        /**
         * 是否使用了LLM兜底
         */
        @JsonProperty("llm_fallback_used")
        private Boolean llmFallbackUsed;

        /**
         * LLM来源
         */
        @JsonProperty("llm_source")
        private String llmSource;

        // 兼容旧版本
        @Deprecated
        private String text;

        @Deprecated
        private JsonNode boxes;

        @Deprecated
        private JsonNode tables;
    }

    /**
     * 文本块
     */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TextBlock implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 文本内容
         */
        private String text;

        /**
         * 置信度
         */
        private Double confidence;

        /**
         * 位置坐标
         */
        private Position position;
    }

    /**
     * 位置坐标
     */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Position implements Serializable {

        private static final long serialVersionUID = 1L;

        private Integer x;
        private Integer y;
        private Integer width;
        private Integer height;
    }

    /**
     * 版面分析结果
     */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class LayoutAnalysis implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 是否包含表格
         */
        @JsonProperty("has_table")
        private Boolean hasTable;

        /**
         * 表格区域列表
         */
        @JsonProperty("table_regions")
        private List<Position> tableRegions;
    }

    /**
     * NLP提取结果
     */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class NlpResult implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * KV键值对数据
         */
        @JsonProperty("kv_data")
        private JsonNode kvData;

        /**
         * 表格数据
         */
        @JsonProperty("table_data")
        private List<JsonNode> tableData;

        /**
         * 是否使用了LLM兜底
         */
        @JsonProperty("llm_fallback_used")
        private Boolean llmFallbackUsed;

        /**
         * LLM来源
         */
        @JsonProperty("llm_source")
        private String llmSource;
    }

    /**
     * 字段提取结果（兼容旧版本）
     */
    @Deprecated
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FieldsResult implements Serializable {

        private static final long serialVersionUID = 1L;

        @JsonProperty("extract_details")
        private JsonNode extractDetails;

        @JsonProperty("extract_main")
        private JsonNode extractMain;

        @JsonProperty("llm_fallback_used")
        private Boolean llmFallbackUsed;

        @JsonProperty("llm_source")
        private String llmSource;
    }
}



