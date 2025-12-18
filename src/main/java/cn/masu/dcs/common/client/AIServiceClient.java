package cn.masu.dcs.common.client;

import cn.masu.dcs.common.config.AiServiceProperties;
import cn.masu.dcs.common.exception.BusinessException;
import cn.masu.dcs.common.result.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * AI服务客户端
 * <p>
 * 支持调用Python AI服务进行OCR和NLP处理，包含LLM兜底功能
 * </p>
 * <p>
 * 主要功能：
 * 1. OCR识别（支持LLM兜底）
 * 2. NLP信息提取（支持LLM兜底）
 * 3. 多模态LLM直接处理
 * 4. 文档分类
 * 5. 智能处理策略选择
 * 6. 成本控制与统计
 * </p>
 *
 * @author zyq
 * @since 2025-12-06
 */
@Slf4j
@Component
public class AIServiceClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final OkHttpClient okHttpClient;

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final String baseUrl;
    private final boolean enableLLMFallback;
    private final double dailyBudget;
    private final double ocrConfidenceThreshold;
    private final double nlpConfidenceThreshold;
    private final String defaultLLMModel;

    /**
     * 当前累计成本
     */
    private double currentCost = 0.0;

    /**
     * 默认构造函数
     */
    public AIServiceClient(AiServiceProperties properties) {
        this.baseUrl = properties.getBaseUrl();
        this.enableLLMFallback = properties.isEnableLlmFallback();
        this.dailyBudget = properties.getDailyBudget();
        this.ocrConfidenceThreshold = properties.getOcrConfidenceThreshold();
        this.nlpConfidenceThreshold = properties.getNlpConfidenceThreshold();
        this.defaultLLMModel = properties.getLlmModel();
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(300, TimeUnit.SECONDS)
            .readTimeout(300, TimeUnit.SECONDS)
            .writeTimeout(300, TimeUnit.SECONDS)
            .callTimeout(Duration.ofSeconds(300))
            .retryOnConnectionFailure(true)
            .build();
    }

    /**
     * 调用Python统一AI处理接口（使用OkHttp）
     * <p>
     * 支持完整的请求/响应日志记录，便于审计和问题排查
     * </p>
     *
     * @param requestBody 请求体
     * @return AI处理结果的完整响应
     * @throws RuntimeException 当调用失败时
     */
    public AIProcessResponse processUnifiedAi(Object requestBody) {
        String url = baseUrl + "/api/v1/ai/process";
        long startTime = System.currentTimeMillis();

        try {
            String json = objectMapper.writeValueAsString(requestBody);
            log.info("发送AI处理请求到: {}, 请求大小: {} bytes", url, json.length());

            Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(json.getBytes(StandardCharsets.UTF_8), JSON))
                .build();

            try (Response response = okHttpClient.newCall(request).execute()) {
                long duration = System.currentTimeMillis() - startTime;

                if (!response.isSuccessful() || response.body() == null) {
                    log.error("AI服务HTTP错误: code={}, duration={}ms", response.code(), duration);
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "AI服务HTTP错误:" + response.code());
                }

                String body = response.body().string();
                JsonNode root = objectMapper.readTree(body);

                int code = root.has("code") ? root.get("code").asInt() : HttpStatus.INTERNAL_SERVER_ERROR.value();
                if (code != 200) {
                    String message = root.has("message") ? root.get("message").asText() : "AI service error";
                    String detail = root.has("detail") && !root.get("detail").isNull()
                        ? root.get("detail").asText()
                        : null;
                    log.error("AI服务业务错误: code={}, message={}, detail={}", code, message, detail);
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), message + (detail != null ? (": " + detail) : ""));
                }

                JsonNode data = root.get("data");
                log.info("AI处理成功, 耗时: {}ms", duration);

                // 构建响应对象
                return AIProcessResponse.builder()
                    .data(data)
                    .processingTime(duration / 1000.0)
                    .success(true)
                    .build();
            }
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("AI处理请求失败, 耗时: {}ms", duration, e);
            return AIProcessResponse.builder()
                .success(false)
                .errorMessage(e.getMessage())
                .processingTime(duration / 1000.0)
                .build();
        }
    }

    /**
     * 调用Python统一AI处理接口（返回JsonNode，保持向后兼容）
     *
     * @param requestBody 请求体
     * @return AI处理结果
     * @throws RuntimeException 当调用失败时
     * @deprecated 使用 {@link #processUnifiedAi(Object)} 代替
     */
    @Deprecated
    public JsonNode processUnifiedAiLegacy(Object requestBody) {
        AIProcessResponse response = processUnifiedAi(requestBody);
        if (!response.getSuccess()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), response.getErrorMessage());
        }
        return response.getData();
    }

    /**
     * OCR识别（带LLM兜底）
     *
     * @param filePath 文件路径
     * @param fileId   文件ID
     * @return OCR识别结果
     */
    public OCRResponse performOCRWithFallback(String filePath, Long fileId) {
        String url = baseUrl + "/api/v1/ocr/recognize-with-ai";
        Map<String, Object> request = buildOCRRequest(filePath, fileId, true);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                url,
                new HttpEntity<>(request, buildHeaders()),
                String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode responseData = objectMapper.readTree(response.getBody());
                return parseOCRResponse(responseData);
            }

            log.error("OCR请求失败: status={}", response.getStatusCode());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "OCR请求失败");

        } catch (Exception e) {
            log.error("OCR请求异常: fileId={}", fileId, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "OCR请求失败", e);
        }
    }

    /**
     * 传统OCR识别（不使用LLM）
     *
     * @param filePath 文件路径
     * @param fileId   文件ID
     * @return OCR识别结果
     */
    public OCRResponse performOCR(String filePath, Long fileId) {
        String url = baseUrl + "/api/v1/ocr/recognize";
        Map<String, Object> request = buildOCRRequest(filePath, fileId, false);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                url,
                new HttpEntity<>(request, buildHeaders()),
                String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode responseData = objectMapper.readTree(response.getBody());
                return parseOCRResponse(responseData);
            }

            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "OCR请求失败");

        } catch (Exception e) {
            log.error("OCR请求失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "OCR请求失败", e);
        }
    }

    /**
     * NLP信息提取（带LLM兜底）
     *
     * @param ocrText        OCR识别文本
     * @param fileId         文件ID
     * @param templateConfig 模板配置
     * @return NLP提取结果
     */
    public NLPResponse extractFieldsWithFallback(String ocrText, Long fileId, Map<String, Object> templateConfig) {
        String url = baseUrl + "/api/v1/nlp/extract-with-llm";

        Map<String, Object> request = new HashMap<>(8);
        request.put("file_id", fileId);
        request.put("ocr_text", ocrText);
        request.put("template_config", templateConfig);
        request.put("llm_model", defaultLLMModel);
        request.put("fallback_threshold", nlpConfidenceThreshold);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                url,
                new HttpEntity<>(request, buildHeaders()),
                String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode responseData = objectMapper.readTree(response.getBody());
                return parseNLPResponse(responseData);
            }

            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "NLP提取失败");

        } catch (Exception e) {
            log.error("NLP提取失败: fileId={}", fileId, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "NLP提取失败", e);
        }
    }

    /**
     * 传统NLP提取（不使用LLM）
     *
     * @param ocrText        OCR识别文本
     * @param fileId         文件ID
     * @param templateConfig 模板配置
     * @return NLP提取结果
     */
    public NLPResponse extractFields(String ocrText, Long fileId, Map<String, Object> templateConfig) {
        String url = baseUrl + "/api/v1/nlp/extract";

        Map<String, Object> request = new HashMap<>(8);
        request.put("file_id", fileId);
        request.put("ocr_text", ocrText);
        request.put("template_config", templateConfig);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                url,
                new HttpEntity<>(request, buildHeaders()),
                String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode responseData = objectMapper.readTree(response.getBody());
                return parseNLPResponse(responseData);
            }

            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "NLP提取失败");

        } catch (Exception e) {
            log.error("NLP提取失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "NLP提取失败", e);
        }
    }

    /**
     * 直接使用多模态LLM处理
     *
     * @param imagePath      图片路径
     * @param fileId         文件ID
     * @param templateConfig 模板配置
     * @return NLP处理结果
     */
    public NLPResponse processWithMultimodalLLM(String imagePath, Long fileId, Map<String, Object> templateConfig) {
        // 检查预算
        if (currentCost >= dailyBudget) {
            log.warn("已达到每日预算上限(¥{}), 拒绝使用LLM", dailyBudget);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "当天预算已用尽");
        }

        String url = baseUrl + "/api/v1/llm/process-image";

        Map<String, Object> request = new HashMap<>(8);
        request.put("file_path", imagePath);
        request.put("file_id", fileId);
        request.put("template_config", templateConfig);
        request.put("model", "gpt-4-vision");
        request.put("prompt_mode", "detailed");

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                url,
                new HttpEntity<>(request, buildHeaders()),
                String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode responseData = objectMapper.readTree(response.getBody());
                NLPResponse nlpResponse = parseNLPResponse(responseData);

                // 更新成本
                if (nlpResponse.getCost() != null) {
                    currentCost += nlpResponse.getCost();
                    log.info("多模态LLM处理完成，成本: ¥{}, 累计: ¥{}", nlpResponse.getCost(), currentCost);
                }

                return nlpResponse;
            }

            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "多模态LLM处理失败");

        } catch (Exception e) {
            log.error("多模态LLM处理失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "多模态LLM处理失败", e);
        }
    }

    /**
     * 文档分类
     *
     * @param text 文档文本
     * @return 分类结果
     */
    public ClassificationResponse classifyDocument(String text) {
        String url = baseUrl + "/api/v1/nlp/classify";

        Map<String, Object> request = new HashMap<>(8);
        request.put("text", text);
        request.put("candidates", new String[]{"成绩单", "请假条", "证明文件", "合同"});

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                url,
                new HttpEntity<>(request, buildHeaders()),
                String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode responseData = objectMapper.readTree(response.getBody());
                return parseClassificationResponse(responseData);
            }

            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "文档分类失败");

        } catch (Exception e) {
            log.error("文档分类失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "文档分类失败", e);
        }
    }

    /**
     * 智能处理文档（自动选择最优策略）
     *
     * @param imagePath      图片路径
     * @param fileId         文件ID
     * @param templateConfig 模板配置
     * @param complexity     文档复杂度
     * @return 处理结果
     */
    public DocumentProcessResult smartProcessDocument(
            String imagePath,
            Long fileId,
            Map<String, Object> templateConfig,
            DocumentComplexity complexity
    ) {
        log.info("智能处理文档: fileId={}, complexity={}", fileId, complexity);

        OCRResponse ocrResponse = null;
        NLPResponse nlpResponse = null;
        String strategy;

        try {
            // 策略1: 简单文档 - 传统方法
            if (complexity == DocumentComplexity.SIMPLE) {
                log.info("使用传统方法处理简单文档");
                ocrResponse = performOCR(imagePath, fileId);
                nlpResponse = extractFields(ocrResponse.getText(), fileId, templateConfig);
                strategy = "traditional";
            }
            // 策略2: 复杂文档且预算充足 - 直接用多模态LLM
            else if (complexity == DocumentComplexity.COMPLEX && currentCost + 0.05 <= dailyBudget) {
                log.info("使用多模态LLM处理复杂文档");
                nlpResponse = processWithMultimodalLLM(imagePath, fileId, templateConfig);
                strategy = "multimodal_llm";
            }
            // 策略3: 普通文档或预算不足 - 使用兜底策略
            else {
                log.info("使用LLM兜底策略处理文档");
                ocrResponse = enableLLMFallback ?
                    performOCRWithFallback(imagePath, fileId) :
                    performOCR(imagePath, fileId);
                nlpResponse = enableLLMFallback ?
                    extractFieldsWithFallback(ocrResponse.getText(), fileId, templateConfig) :
                    extractFields(ocrResponse.getText(), fileId, templateConfig);
                strategy = enableLLMFallback ? "fallback" : "traditional";
            }

            return DocumentProcessResult.builder()
                .ocrResponse(ocrResponse)
                .nlpResponse(nlpResponse)
                .strategy(strategy)
                .success(true)
                .build();

        } catch (Exception e) {
            log.error("智能处理文档失败: fileId={}", fileId, e);
            return DocumentProcessResult.builder()
                .success(false)
                .errorMessage(e.getMessage())
                .build();
        }
    }

    /**
     * 获取成本统计
     *
     * @return 成本统计信息
     */
    public CostStatistics getCostStatistics() {
        return CostStatistics.builder()
            .currentCost(currentCost)
            .dailyBudget(dailyBudget)
            .remainingBudget(dailyBudget - currentCost)
            .usagePercentage(currentCost / dailyBudget * 100)
            .build();
    }

    /**
     * 重置每日成本
     */
    public void resetDailyCost() {
        this.currentCost = 0.0;
        log.info("每日成本已重置");
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 构建OCR请求
     */
    private Map<String, Object> buildOCRRequest(String filePath, Long fileId, boolean useLLM) {
        Map<String, Object> request = new HashMap<>(8);
        request.put("file_path", filePath);
        request.put("file_id", fileId);

        if (useLLM) {
            request.put("ai_model", defaultLLMModel);
            request.put("fallback_threshold", ocrConfidenceThreshold);
        }

        Map<String, Object> options = new HashMap<>(8);
        options.put("language", "ch_en");
        options.put("enhance_image", true);
        options.put("detect_table", true);
        options.put("detect_layout", true);
        request.put("options", options);

        return request;
    }

    /**
     * 构建HTTP请求头
     */
    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        return headers;
    }

    /**
     * 解析OCR响应
     */
    private OCRResponse parseOCRResponse(JsonNode responseData) {
        JsonNode data = responseData.get("data");

        OCRResponse response = new OCRResponse();
        response.setFileId(data.get("file_id").asLong());

        // 检查是否使用了LLM兜底
        if (data.has("fallback_result") && data.get("fallback_result").get("used").asBoolean()) {
            JsonNode fallbackResult = data.get("fallback_result");
            response.setUsedLLMFallback(true);
            response.setLlmSource(fallbackResult.get("source").asText());
            response.setCost(fallbackResult.get("model_cost").asDouble());

            JsonNode finalResult = data.get("final_result");
            response.setText(finalResult.get("text").asText());
            response.setConfidence(finalResult.get("confidence").asDouble());
            response.setSource(finalResult.get("source").asText());

            // 更新成本
            currentCost += response.getCost();
            log.info("OCR使用LLM兜底: source={}, cost=¥{}", response.getLlmSource(), response.getCost());
        } else {
            // 使用PaddleOCR结果
            JsonNode results = data.get("results").get(0);
            response.setText(results.get("text").asText());
            response.setConfidence(results.get("confidence").asDouble());
            response.setSource("paddleocr");
            response.setUsedLLMFallback(false);

            log.info("OCR使用传统方法: confidence={}", response.getConfidence());
        }

        return response;
    }

    /**
     * 解析NLP响应
     */
    private NLPResponse parseNLPResponse(JsonNode responseData) {
        JsonNode data = responseData.get("data");

        NLPResponse response = new NLPResponse();
        response.setFileId(data.has("file_id") ? data.get("file_id").asLong() : null);

        // 检查是否使用了LLM兜底
        if (data.has("fallback_result") && data.get("fallback_result").get("used").asBoolean()) {
            JsonNode fallbackResult = data.get("fallback_result");
            response.setUsedLLMFallback(true);
            response.setLlmSource(fallbackResult.get("source").asText());
            response.setCost(fallbackResult.get("model_cost").asDouble());
            response.setExtractDetails(fallbackResult.get("extract_details"));

            // 更新成本
            currentCost += response.getCost();
            log.info("NLP使用LLM兜底: source={}, cost=¥{}", response.getLlmSource(), response.getCost());
        } else {
            // 使用BERT结果
            response.setExtractDetails(data.get("extract_details"));
            response.setUsedLLMFallback(false);
            response.setSource("bert");

            log.info("NLP使用传统方法");
        }

        // 处理直接使用多模态LLM的情况
        if (data.has("cost")) {
            response.setCost(data.get("cost").asDouble());
        }

        return response;
    }

    /**
     * 解析分类响应
     */
    private ClassificationResponse parseClassificationResponse(JsonNode responseData) {
        JsonNode data = responseData.get("data");

        ClassificationResponse response = new ClassificationResponse();
        response.setDocumentType(data.get("document_type").asText());
        response.setConfidence(data.get("confidence").asDouble());

        if (data.has("probabilities")) {
            response.setProbabilities(data.get("probabilities"));
        }

        return response;
    }

    // ==================== 内部类定义 ====================

    /**
     * AI处理响应（包含完整的处理信息）
     */
    @Data
    @Builder
    public static class AIProcessResponse {
        /**
         * 响应数据
         */
        private JsonNode data;

        /**
         * 处理耗时（秒）
         */
        private Double processingTime;

        /**
         * 是否成功
         */
        private Boolean success;

        /**
         * 错误信息（失败时）
         */
        private String errorMessage;

        /**
         * HTTP状态码
         */
        private Integer httpStatus;

        /**
         * 请求ID（用于追踪）
         */
        private String requestId;
    }

    /**
     * OCR响应
     */
    @Data
    public static class OCRResponse {
        private Long fileId;
        private String text;
        private Double confidence;
        private String source;
        private Boolean usedLLMFallback = false;
        private String llmSource;
        private Double cost = 0.0;
        private JsonNode boxes;
        private JsonNode tables;
    }

    /**
     * NLP响应
     */
    @Data
    public static class NLPResponse {
        private Long fileId;
        private JsonNode extractDetails;
        private String source;
        private Boolean usedLLMFallback = false;
        private String llmSource;
        private Double cost = 0.0;
    }

    /**
     * 分类响应
     */
    @Data
    public static class ClassificationResponse {
        private String documentType;
        private Double confidence;
        private JsonNode probabilities;
    }

    /**
     * 文档处理结果
     */
    @Data
    @Builder
    public static class DocumentProcessResult {
        private OCRResponse ocrResponse;
        private NLPResponse nlpResponse;
        private String strategy;
        private Boolean success;
        private String errorMessage;
    }

    /**
     * 成本统计
     */
    @Data
    @Builder
    public static class CostStatistics {
        private Double currentCost;
        private Double dailyBudget;
        private Double remainingBudget;
        private Double usagePercentage;
    }

    /**
     * 文档复杂度枚举
     */
    public enum DocumentComplexity {
        /** 简单文档（清晰、标准格式） */
        SIMPLE,
        /** 普通文档 */
        NORMAL,
        /** 复杂文档（手写、低质量、复杂版面） */
        COMPLEX
    }
}
