package cn.masu.dcs.controller;

import cn.masu.dcs.common.client.AIServiceClient;
import cn.masu.dcs.common.config.MinioConfig;
import cn.masu.dcs.common.result.R;
import cn.masu.dcs.common.util.MinioUtils;
import cn.masu.dcs.common.util.SnowflakeIdGenerator;
import cn.masu.dcs.entity.DocumentExtractDetail;
import cn.masu.dcs.entity.DocumentExtractMain;
import cn.masu.dcs.entity.DocumentFile;
import cn.masu.dcs.entity.SysUser;
import cn.masu.dcs.mapper.DocumentExtractDetailMapper;
import cn.masu.dcs.mapper.DocumentExtractMainMapper;
import cn.masu.dcs.mapper.DocumentFileMapper;
import cn.masu.dcs.mapper.SysUserMapper;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * AI处理控制器
 * <p>
 * 提供统一的AI文档处理接口，支持OCR、NLP、LLM等多种模式
 * </p>
 *
 * @author zyq
 * @since 2025-12-06
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiProcessController {

    private final AIServiceClient aiServiceClient;
    private final ObjectMapper objectMapper;
    private final MinioUtils minioUtils;
    private final MinioConfig minioConfig;
    private final SnowflakeIdGenerator idGenerator;
    private final DocumentFileMapper fileMapper;
    private final DocumentExtractMainMapper extractMainMapper;
    private final DocumentExtractDetailMapper extractDetailMapper;
    private final SysUserMapper userMapper;

    /**
     * 统一AI处理接口
     * <p>
     * 对应前端的 /api/v1/ai/process 接口
     * 支持多种处理模式：
     * 1. 仅LLM模式（llm_only=true）
     * 2. 禁用AI模式（disable_ai=true）
     * 3. LLM兜底模式（use_llm=true）
     * 4. 多模态LLM模式（llm_image=true）
     * <p>
     * 功能增强：
     * - 自动保存上传的文件到MinIO
     * - 创建文件记录到数据库
     * - 将AI处理结果与文件绑定
     * </p>
     *
     * @param request 处理请求
     * @return AI处理结果（包含fileId）
     */
    @PostMapping("/process")
    public R<Map<String, Object>> processDocument(@RequestBody AiProcessRequest request) {
        log.info("收到AI处理请求");

        // 兜底防御
        if (request == null) {
            log.warn("请求体为空");
            return R.fail(400, "请求参数错误：请求体不能为空");
        }

        Long requestFileId = request.getFileId();
        String fileContent = request.getFileContent();
        String text = request.getText();

        // 参数校验：至少要有文件ID、文件内容或文本内容之一
        boolean hasFileId = (requestFileId != null && requestFileId > 0);
        boolean hasFileContent = (fileContent != null && !fileContent.trim().isEmpty());
        boolean hasText = (text != null && !text.trim().isEmpty());

        if (!hasFileId && !hasFileContent && !hasText) {
            log.warn("请求参数无效：缺少文件ID、文件内容或文本");
            return R.fail(400, "请求参数错误：必须提供文件ID(fileId)、文件内容(fileContent)或文本内容(text)");
        }

        Long fileId = requestFileId;  // 使用请求中的fileId
        String bucketName = null;
        String objectName = null;
        String fileUrl = null;

        try {
            // ===================== 1. 处理文件 =====================
            if (hasFileId) {
                // 1.1 使用已上传的文件ID
                log.info("使用已上传的文件ID: {}", fileId);
                DocumentFile existingFile = fileMapper.selectById(fileId);
                if (existingFile == null) {
                    log.error("文件不存在: fileId={}", fileId);
                    return R.fail(404, "文件不存在：fileId=" + fileId);
                }

                bucketName = existingFile.getMinioBucket();
                objectName = existingFile.getMinioObject();
                fileUrl = buildMinioUrl(bucketName, objectName);

                log.info("文件信息: fileName={}, bucket={}, object={}",
                    existingFile.getFileName(), bucketName, objectName);

            } else if (hasFileContent) {
                // 1.2 保存新的文件内容到MinIO + 数据库
                try {
                    log.info("检测到文件内容，开始保存到 MinIO 和数据库");
                    fileId = saveFileToMinioAndDatabase(request);
                    log.info("文件已保存: fileId={}", fileId);

                    // 从数据库查询文件存储信息
                    DocumentFile savedFile = fileMapper.selectById(fileId);
                    if (savedFile == null) {
                        log.error("根据 fileId={} 未查询到 DocumentFile 记录", fileId);
                        return R.fail(500, "文件保存异常：未找到文件记录");
                    }

                    bucketName = savedFile.getMinioBucket();
                    objectName = savedFile.getMinioObject();
                    fileUrl = buildMinioUrl(bucketName, objectName);

                    log.info("文件存储信息: bucket={}, object={}", bucketName, objectName);
                } catch (Exception e) {
                    log.error("文件保存失败", e);
                    return R.fail(500, "文件保存失败，请稍后重试或联系管理员");
                }
            }
            // 如果只有text，fileId保持为null

            // ===================== 2. 构建 Python AI 请求体（包含文件元信息） =====================
            Map<String, Object> pythonRequest = buildPythonRequest(request, fileId, bucketName, objectName, fileUrl);
            log.info("构建发往 Python 的请求体: {}", pythonRequest);

            // ===================== 3. 调用 Python AI 服务 =====================
            log.info("开始调用 Python AI 服务");
            AIServiceClient.AIProcessResponse aiResponse = aiServiceClient.processUnifiedAi(pythonRequest);
            log.info("Python AI 服务调用完成，原始响应: {}", aiResponse);

            // 防御性检查
            if (aiResponse == null) {
                log.error("AI服务返回结果为 null");
                return R.fail(500, "AI处理失败：AI服务无返回结果");
            }
            if (Boolean.FALSE.equals(aiResponse.getSuccess())) {
                log.error("AI处理失败: {}", aiResponse.getErrorMessage());
                return R.fail(500, "AI处理失败：" + Optional.ofNullable(aiResponse.getErrorMessage())
                        .orElse("未知错误"));
            }

            JsonNode aiResult = aiResponse.getData();
            log.info("AI处理成功，开始处理结果持久化逻辑");

            // ===================== 4. 如果有文件ID，保存 AI 结果到数据库 =====================
            if (fileId != null) {
                try {
                    saveAiResultToDatabase(fileId, aiResult, fileUrl);
                    log.info("AI处理结果已保存到数据库: fileId={}", fileId);
                } catch (Exception e) {
                    // 这里失败不影响返回，只记日志
                    log.error("保存 AI 结果到数据库失败, fileId={}", fileId, e);
                }
            }

            // ===================== 5. 组装返回结果 =====================
            Map<String, Object> result = new HashMap<>(8);
            // 纯文本场景下 fileId 可能为 null，前端要做判空
            result.put("fileId", fileId);
            result.put("aiResult", aiResult);

            if (bucketName != null && objectName != null) {
                Map<String, Object> fileInfo = new HashMap<>(4);
                fileInfo.put("bucket", bucketName);
                fileInfo.put("object", objectName);
                fileInfo.put("url", fileUrl);
                fileInfo.put("fileId", fileId);
                result.put("fileInfo", fileInfo);
            }

            log.info("AI处理完成并返回，fileId={}", fileId);
            return R.ok(result);
        } catch (Exception e) {
            // 兜底异常捕获，防止接口直接抛 500 堆栈
            log.error("AI处理接口内部异常", e);
            return R.fail(500, "AI处理失败，请稍后重试或联系管理员");
        }
    }


    /**
     * 文件上传处理接口
     * <p>
     * 支持上传文件进行处理，适配multipart/form-data格式
     * </p>
     *
     * @param file               上传的文件
     * @param optionsJson        处理选项JSON字符串
     * @param templateConfigJson 模板配置JSON字符串
     * @return AI处理结果
     */
    @PostMapping("/process-file")
    public R<Map<String, Object>> processFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "options", required = false) String optionsJson,
            @RequestParam(value = "template_config", required = false) String templateConfigJson) {

        log.info("收到文件上传请求: fileName={}, size={}", file.getOriginalFilename(), file.getSize());

        try {
            // 将文件转为Base64
            byte[] bytes = file.getBytes();
            String base64 = java.util.Base64.getEncoder().encodeToString(bytes);
            String fileContent = "data:" + file.getContentType() + ";base64," + base64;

            // 构建请求
            AiProcessRequest request = new AiProcessRequest();
            request.setFileContent(fileContent);
            request.setFileName(file.getOriginalFilename());

            if (optionsJson != null) {
                request.setOptions(objectMapper.readValue(optionsJson, ProcessOptions.class));
            }
            if (templateConfigJson != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> templateConfig = objectMapper.readValue(templateConfigJson, Map.class);
                request.setTemplateConfig(templateConfig);
            }

            // 调用处理逻辑
            return processDocument(request);

        } catch (Exception e) {
            log.error("文件处理失败", e);
            return R.fail(500, "文件处理失败: " + e.getMessage());
        }
    }

    /**
     * 获取AI服务状态
     *
     * @return 服务状态信息
     */
    @GetMapping("/status")
    public R<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>(8);
        status.put("service", "AI Document Assistant");
        status.put("status", "ready");
        status.put("version", "1.0.0");
        status.put("backend", "Python AI Service");

        // 获取成本统计
        try {
            AIServiceClient.CostStatistics costStats = aiServiceClient.getCostStatistics();
            status.put("cost", Map.of(
                    "current", costStats.getCurrentCost(),
                    "budget", costStats.getDailyBudget(),
                    "remaining", costStats.getRemainingBudget(),
                    "usage_percentage", costStats.getUsagePercentage()
            ));
        } catch (Exception e) {
            log.warn("获取成本统计失败", e);
        }

        return R.ok(status);
    }

    /**
     * 重置每日成本
     *
     * @return 操作结果
     */
    @PostMapping("/reset-cost")
    public R<String> resetDailyCost() {
        try {
            aiServiceClient.resetDailyCost();
            return R.ok("每日成本已重置");
        } catch (Exception e) {
            log.error("重置成本失败", e);
            return R.fail("重置失败: " + e.getMessage());
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 构建Python后端请求体
     */
//    private Map<String, Object> buildPythonRequest(AiProcessRequest request) {
//        Map<String, Object> pythonRequest = new HashMap<>(8);
//
//        // 基本字段
//        if (request.getFileContent() != null) {
//            pythonRequest.put("file_content", request.getFileContent());
//        }
//        if (request.getFileName() != null) {
//            pythonRequest.put("file_name", request.getFileName());
//        }
//        if (request.getText() != null) {
//            pythonRequest.put("text", request.getText());
//        }
//
//        // 处理选项
//        ProcessOptions options = request.getOptions();
//        if (options != null) {
//            Map<String, Object> optionsMap = new HashMap<>(32);
//
//            // 图像处理选项
//            optionsMap.put("enhance_image", options.getEnhanceImage() != null ? options.getEnhanceImage() : true);
//            optionsMap.put("handwriting_mode", options.getHandwritingMode() != null ? options.getHandwritingMode() : false);
//
//            // AI模式控制
//            optionsMap.put("llm_only", options.getLlmOnly() != null ? options.getLlmOnly() : false);
//            optionsMap.put("disable_ai", options.getDisableAi() != null ? options.getDisableAi() : false);
//            optionsMap.put("use_llm", options.getUseLlm() != null ? options.getUseLlm() : true);
//            optionsMap.put("llm_image", options.getLlmImage() != null ? options.getLlmImage() : false);
//
//            // NLP选项
//            optionsMap.put("auto_infer_fields", options.getAutoInferFields() != null ? options.getAutoInferFields() : true);
//            optionsMap.put("analyze_ocr", options.getAnalyzeOcr() != null ? options.getAnalyzeOcr() : true);
//
//            // 表格检测
//            optionsMap.put("detect_table", options.getDetectTable() != null ? options.getDetectTable() : true);
//            optionsMap.put("table_force", options.getTableForce() != null ? options.getTableForce() : false);
//            if (options.getTableDetectThreshold() != null) {
//                optionsMap.put("table_detect_threshold", options.getTableDetectThreshold());
//            }
//
//            // LLM配置
//            if (options.getLlmProvider() != null) {
//                optionsMap.put("llm_provider", options.getLlmProvider());
//            }
//            if (options.getLlmModel() != null) {
//                optionsMap.put("llm_model", options.getLlmModel());
//            }
//
//            pythonRequest.put("options", optionsMap);
//        }
//
//        // 模板配置
//        if (request.getTemplateConfig() != null) {
//            pythonRequest.put("template_config", request.getTemplateConfig());
//        } else {
//            pythonRequest.put("template_config", new HashMap<>(4));
//        }
//
//        return pythonRequest;
//    }

    /**
     * 构建发往 Python 统一 AI 服务的请求体
     */
    private Map<String, Object> buildPythonRequest(AiProcessRequest request,
                                                   Long fileId,
                                                   String bucketName,
                                                   String objectName,
                                                   String fileUrl) {
        Map<String, Object> pythonRequest = new HashMap<>(8);

        // 原有字段（按你原来的协议来）
        if (request.getText() != null && !request.getText().trim().isEmpty()) {
            pythonRequest.put("text", request.getText().trim());
        }
        if (request.getFileContent() != null && !request.getFileContent().trim().isEmpty()) {
            pythonRequest.put("file_content", request.getFileContent().trim());
        }
        if (request.getOptions() != null) {
            pythonRequest.put("options", request.getOptions());
        }

        // 新增：文件元信息，方便 Python 侧直接从 MinIO 拉取
        if (fileId != null || bucketName != null || objectName != null || fileUrl != null) {
            Map<String, Object> fileMeta = new HashMap<>(4);
            fileMeta.put("file_id", fileId);
            fileMeta.put("bucket", bucketName);
            fileMeta.put("object", objectName);
            fileMeta.put("url", fileUrl);
            pythonRequest.put("file_meta", fileMeta);
        }

        return pythonRequest;
    }

    /**
     * 保存文件到MinIO和数据库
     *
     * @param request AI处理请求
     * @return 文件ID
     */
    private Long saveFileToMinioAndDatabase(AiProcessRequest request) throws Exception {
        // 解析Base64文件内容
        String fileContent = request.getFileContent();
        String fileName = request.getFileName();

        // 从Base64中提取文件类型和内容
        // 格式: data:image/png;base64,iVBORw0KGgo...
        String base64Data;
        String contentType = "application/octet-stream";

        if (fileContent.contains(",")) {
            String[] parts = fileContent.split(",", 2);
            base64Data = parts[1];

            // 提取content type
            if (parts[0].contains(":") && parts[0].contains(";")) {
                String typeInfo = parts[0].substring(parts[0].indexOf(":") + 1, parts[0].indexOf(";"));
                contentType = typeInfo;
            }
        } else {
            base64Data = fileContent;
        }

        // 解码Base64
        byte[] fileBytes = Base64.getDecoder().decode(base64Data);

        // 确定文件扩展名
        String fileExtension = "bin";
        if (fileName != null && fileName.contains(".")) {
            fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        } else if (contentType.contains("pdf")) {
            fileExtension = "pdf";
        } else if (contentType.contains("image/jpeg") || contentType.contains("image/jpg")) {
            fileExtension = "jpg";
        } else if (contentType.contains("image/png")) {
            fileExtension = "png";
        }

        // 上传到MinIO
        String bucketName = minioConfig.getBucketName();

        try (InputStream inputStream = new ByteArrayInputStream(fileBytes)) {
            // 生成对象名称（使用日期路径）
            String datePath = java.time.LocalDate.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String uuid = java.util.UUID.randomUUID().toString();
            String objectName = String.format("%s/%s.%s", datePath, uuid, fileExtension);

            // 上传文件
            minioUtils.uploadFile(
                    bucketName,
                    objectName,
                    inputStream,
                    (long) fileBytes.length,
                    contentType
            );

            log.info("文件上传到MinIO成功: bucket={}, object={}", bucketName, objectName);

            // 获取当前用户ID
            Long userId = getCurrentUserId();

            // 如果无法获取当前用户ID，查询数据库中的第一个用户
            if (userId == null) {
                userId = getDefaultUserId();
                log.warn("未获取到当前用户ID，使用默认用户ID: {}", userId);
            }

            // 创建文件记录
            DocumentFile documentFile = new DocumentFile();
            documentFile.setId(idGenerator.nextId());
            documentFile.setFileName(fileName != null ? fileName : "document." + fileExtension);
            documentFile.setMinioBucket(bucketName);
            documentFile.setMinioObject(objectName);
            documentFile.setFileType(fileExtension);
            documentFile.setFileSize((long) fileBytes.length);
            documentFile.setUserId(userId);
            documentFile.setProcessStatus(1); // 1=已入队，等待AI处理
            documentFile.setProcessMode("AI_PROCESS");
            documentFile.setRetryCount(0);
            documentFile.setDeleted(0);
            documentFile.setVersion(0);

            fileMapper.insert(documentFile);

            log.info("文件记录已保存到数据库: fileId={}", documentFile.getId());

            return documentFile.getId();
        }
    }

    /**
     * 保存AI处理结果到数据库
     * <p>
     * 完整解析Python返回的所有数据：
     * - document_type: 文档类型
     * - confidence_overall: 整体置信度
     * - basic_info: 基本信息（姓名、学号、学校等）
     * - academic_info: 学业信息
     * - certificate_info: 证书信息
     * - financial_info: 财务信息
     * - leave_info: 请假信息
     * - courses: 课程成绩列表
     * - summary: 摘要
     * - text: 原始OCR文本
     * - tables: 表格数据
     * - fields: 其他字段
     * </p>
     *
     * @param fileId   文件ID
     * @param aiResult AI处理结果
     * @param fileUrl  文件访问URL（可选）
     */
    private void saveAiResultToDatabase(Long fileId, JsonNode aiResult, String fileUrl) {
        try {
            log.info("开始保存AI结果到数据库: fileId={}", fileId);

            // ============ 1. 提取基础信息 ============
            String documentType = getStringFromJson(aiResult, "document_type");
            Double confidence = getDoubleFromJson(aiResult, "confidence_overall");
            String rawText = getStringFromJson(aiResult, "text");
            String summary = getStringFromJson(aiResult, "summary");

            log.info("📋 文档类型: {}, 置信度: {}", documentType, confidence);

            // 创建提取主表记录
            DocumentExtractMain extractMain = new DocumentExtractMain();
            extractMain.setId(idGenerator.nextId());
            extractMain.setFileId(fileId);

            // ============ 2. 保存文档类型和URL ============
            extractMain.setDocumentType(documentType);
            extractMain.setDocumentUrl(fileUrl);
            extractMain.setRawText(rawText);
            extractMain.setSummary(summary);
            extractMain.setConfidence(confidence);

            // ============ 3. 保存完整的AI结果JSON（所有数据） ============
            try {
                String fullResultJson = objectMapper.writeValueAsString(aiResult);
                extractMain.setExtractResult(fullResultJson);
                log.info("✅ 保存完整AI结果JSON，长度: {} 字节", fullResultJson.length());
            } catch (Exception e) {
                log.error("❌ 序列化完整AI结果失败", e);
            }

            // ============ 4. 保存基本信息（basic_info）============
            if (aiResult.has("basic_info") && aiResult.get("basic_info").isObject()) {
                JsonNode basicInfo = aiResult.get("basic_info");
                try {
                    String basicInfoJson = objectMapper.writeValueAsString(basicInfo);
                    extractMain.setBasicInfoJson(basicInfoJson);

                    // 提取关键字段到主表
                    String name = getStringFromJson(basicInfo, "name");
                    String studentId = getStringFromJson(basicInfo, "student_id");
                    String idNumber = getStringFromJson(basicInfo, "id_number");

                    if (name != null && !name.isEmpty()) {
                        extractMain.setOwnerName(name);
                    }
                    if (studentId != null && !studentId.isEmpty()) {
                        extractMain.setOwnerId(studentId);
                    } else if (idNumber != null && !idNumber.isEmpty()) {
                        extractMain.setOwnerId(idNumber);
                    }

                    log.info("✅ 保存基本信息: name={}, studentId={}", name, studentId);
                } catch (Exception e) {
                    log.error("❌ 序列化基本信息失败", e);
                }
            }

            // ============ 5. 保存学业信息（academic_info）============
            if (aiResult.has("academic_info") && aiResult.get("academic_info").isObject()) {
                try {
                    String academicInfoJson = objectMapper.writeValueAsString(aiResult.get("academic_info"));
                    extractMain.setAcademicInfoJson(academicInfoJson);
                    log.info("✅ 保存学业信息");
                } catch (Exception e) {
                    log.error("❌ 序列化学业信息失败", e);
                }
            }

            // ============ 6. 保存证书信息（certificate_info）============
            if (aiResult.has("certificate_info") && aiResult.get("certificate_info").isObject()) {
                try {
                    String certificateInfoJson = objectMapper.writeValueAsString(aiResult.get("certificate_info"));
                    extractMain.setCertificateInfoJson(certificateInfoJson);
                    log.info("✅ 保存证书信息");
                } catch (Exception e) {
                    log.error("❌ 序列化证书信息失败", e);
                }
            }

            // ============ 7. 保存财务信息（financial_info）============
            if (aiResult.has("financial_info") && aiResult.get("financial_info").isObject()) {
                try {
                    String financialInfoJson = objectMapper.writeValueAsString(aiResult.get("financial_info"));
                    extractMain.setFinancialInfoJson(financialInfoJson);
                    log.info("✅ 保存财务信息");
                } catch (Exception e) {
                    log.error("❌ 序列化财务信息失败", e);
                }
            }

            // ============ 8. 保存请假信息（leave_info）============
            if (aiResult.has("leave_info") && aiResult.get("leave_info").isObject()) {
                try {
                    String leaveInfoJson = objectMapper.writeValueAsString(aiResult.get("leave_info"));
                    extractMain.setLeaveInfoJson(leaveInfoJson);
                    log.info("✅ 保存请假信息");
                } catch (Exception e) {
                    log.error("❌ 序列化请假信息失败", e);
                }
            }

            // ============ 9. 保存表格数据（tables）============
            if (aiResult.has("tables") && aiResult.get("tables").isArray()) {
                try {
                    String tablesJson = objectMapper.writeValueAsString(aiResult.get("tables"));
                    extractMain.setTablesJson(tablesJson);
                    log.info("✅ 保存表格数据");
                } catch (Exception e) {
                    log.error("❌ 序列化表格数据失败", e);
                }
            }

            // ============ 10. 提取并保存KV键值对数据（fields）============
            Map<String, Object> kvData = new HashMap<>();

            // 从 fields 提取
            if (aiResult.has("fields") && aiResult.get("fields").isObject()) {
                JsonNode fields = aiResult.get("fields");
                fields.fields().forEachRemaining(entry -> {
                    String key = entry.getKey();
                    JsonNode value = entry.getValue();
                    if (value.isTextual()) {
                        kvData.put(key, value.asText());
                    } else if (value.isNumber()) {
                        kvData.put(key, value.numberValue());
                    } else if (value.isBoolean()) {
                        kvData.put(key, value.asBoolean());
                    } else if (!value.isNull()) {
                        kvData.put(key, value.toString());
                    }
                });
                log.info("✅ 从 fields 提取了 {} 个字段", kvData.size());
            }

            // 保存KV数据为JSON
            if (!kvData.isEmpty()) {
                try {
                    String kvDataJson = objectMapper.writeValueAsString(kvData);
                    extractMain.setKvDataJson(kvDataJson);
                    log.info("✅ 保存 KV 数据JSON，字段数: {}", kvData.size());
                } catch (Exception e) {
                    log.error("❌ 序列化KV数据失败", e);
                }
            }

            // ============ 11. 如果还没有提取到owner信息，从其他位置尝试提取 ============
            if (extractMain.getOwnerName() == null || extractMain.getOwnerName().isEmpty()) {
                // 尝试从kvData提取
                if (kvData.containsKey("name")) {
                    extractMain.setOwnerName(kvData.get("name").toString());
                } else if (kvData.containsKey("student_name")) {
                    extractMain.setOwnerName(kvData.get("student_name").toString());
                }
            }
            if (extractMain.getOwnerId() == null || extractMain.getOwnerId().isEmpty()) {
                if (kvData.containsKey("student_id")) {
                    extractMain.setOwnerId(kvData.get("student_id").toString());
                } else if (kvData.containsKey("id_number")) {
                    extractMain.setOwnerId(kvData.get("id_number").toString());
                }
            }

            extractMain.setStatus(0); // 0=待审核

            // 保存主表记录
            extractMainMapper.insert(extractMain);
            log.info("✅ 保存提取主表成功: extractId={}, docType={}, ownerId={}, ownerName={}",
                extractMain.getId(), extractMain.getDocumentType(),
                extractMain.getOwnerId(), extractMain.getOwnerName());

            // ============ 12. 提取课程详细数据（courses）============
            if (aiResult.has("courses") && aiResult.get("courses").isArray()) {
                JsonNode courses = aiResult.get("courses");
                int rowIndex = 0;
                for (JsonNode course : courses) {
                    DocumentExtractDetail detail = new DocumentExtractDetail();
                    detail.setId(idGenerator.nextId());
                    detail.setFileId(fileId);
                    detail.setMainId(extractMain.getId());
                    detail.setRowIndex(rowIndex++);
                    detail.setRowDataJson(course.toString());
                    detail.setIsVerified(0); // 0=未验证

                    // 提取课程名称和成绩作为字段名和字段值
                    if (course.has("course") || course.has("course_name")) {
                        String courseName = course.has("course") ?
                            course.get("course").asText() : course.get("course_name").asText();
                        detail.setFieldName(courseName);
                    }
                    if (course.has("score") || course.has("grade")) {
                        String score = course.has("score") ?
                            course.get("score").asText() : course.get("grade").asText();
                        detail.setFieldValue(score);
                    }

                    extractDetailMapper.insert(detail);
                }
                log.info("✅ 保存课程详情成功: {} 条记录", rowIndex);
            }

            // ============ 13. 更新文件状态 ============
            DocumentFile file = fileMapper.selectById(fileId);
            if (file != null) {
                // 根据置信度决定状态
                if (confidence != null && confidence > 0.85) {
                    file.setProcessStatus(4); // 4=已归档（高置信度自动归档）
                    log.info("✅ 高置信度({})，自动归档", confidence);
                } else {
                    file.setProcessStatus(3); // 3=待人工（需要人工审核）
                    log.info("⚠️ 低置信度({})，需要人工审核", confidence);
                }
                fileMapper.updateById(file);
            }

            log.info("🎉 AI结果保存完成: fileId={}, extractId={}, documentType={}",
                fileId, extractMain.getId(), documentType);

        } catch (Exception e) {
            log.error("❌ 保存AI结果到数据库失败: fileId={}", fileId, e);
            throw new RuntimeException("保存AI结果失败: " + e.getMessage(), e);
        }
    }

    /**
     * 保存AI处理结果到数据库（兼容旧版本调用）
     *
     * @param fileId   文件ID
     * @param aiResult AI处理结果
     */
    private void saveAiResultToDatabase(Long fileId, JsonNode aiResult) {
        saveAiResultToDatabase(fileId, aiResult, null);
    }

    /**
     * 从JsonNode中安全获取字符串值
     */
    private String getStringFromJson(JsonNode node, String field) {
        if (node == null || !node.has(field) || node.get(field).isNull()) {
            return null;
        }
        return node.get(field).asText();
    }

    /**
     * 从JsonNode中安全获取Double值
     */
    private Double getDoubleFromJson(JsonNode node, String field) {
        if (node == null || !node.has(field) || node.get(field).isNull()) {
            return null;
        }
        return node.get(field).asDouble();
    }

    /**
     * 获取当前登录用户ID
     *
     * @return 用户ID，如果获取失败返回null
     */
    private Long getCurrentUserId() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> userMap = (Map<String, Object>) principal;
                Object userIdObj = userMap.get("userId");
                if (userIdObj instanceof Long) {
                    return (Long) userIdObj;
                } else if (userIdObj instanceof Integer) {
                    return ((Integer) userIdObj).longValue();
                } else if (userIdObj != null) {
                    return Long.parseLong(userIdObj.toString());
                }
            }
        } catch (Exception e) {
            log.warn("获取当前用户ID失败: {}", e.getMessage());
        }

        // 返回null，让调用方决定如何处理
        return null;
    }

    /**
     * 获取默认用户ID（从数据库查询第一个用户）
     *
     * @return 默认用户ID
     */
    private Long getDefaultUserId() {
        try {
            // 查询数据库中的第一个用户
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser> wrapper =
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            wrapper.orderByAsc(SysUser::getId).last("LIMIT 1");

            SysUser user = userMapper.selectOne(wrapper);

            if (user != null) {
                log.info("使用数据库中的第一个用户: userId={}, username={}", user.getId(), user.getUsername());
                return user.getId();
            }
        } catch (Exception e) {
            log.error("查询默认用户失败", e);
        }

        throw new RuntimeException("无法获取有效的用户ID，请先创建用户或登录系统");
    }

    /**
     * 构建MinIO文件URL
     */
    private String buildMinioUrl(String bucket, String object) {
        try {
            return minioUtils.getPresignedObjectUrl(bucket, object);
        } catch (Exception e) {
            log.warn("构建MinIO URL失败", e);
            return String.format("minio://%s/%s", bucket, object);
        }
    }

    // ==================== 内部类 ====================

    /**
     * AI处理请求
     */
    @Data
    public static class AiProcessRequest {
        /**
         * 文件ID（已上传的文件）
         */
        private Long fileId;

        /**
         * Base64编码的文件内容
         */
        @JsonProperty("file_content")  // ✅ 映射JSON中的下划线命名
        private String fileContent;

        /**
         * 文件名
         */
        @JsonProperty("file_name")  // ✅ 映射JSON中的下划线命名
        private String fileName;

        /**
         * 文本内容（可选，用于直接处理文本）
         */
        private String text;

        /**
         * 处理选项
         */
        private ProcessOptions options;

        /**
         * 模板配置
         */
        @JsonProperty("template_config")  // ✅ 映射JSON中的下划线命名
        private Map<String, Object> templateConfig;
    }

    /**
     * 处理选项
     */
    @Data
    public static class ProcessOptions {
        /**
         * 图像增强
         */
        @JsonProperty("enhance_image")
        private Boolean enhanceImage;

        /**
         * 手写增强模式
         */
        @JsonProperty("handwriting_mode")
        private Boolean handwritingMode;

        /**
         * 仅使用LLM（跳过OCR/NLP）
         */
        @JsonProperty("llm_only")
        private Boolean llmOnly;

        /**
         * 禁用AI（仅使用传统方法）
         */
        @JsonProperty("disable_ai")
        private Boolean disableAi;

        /**
         * 允许LLM兜底
         */
        @JsonProperty("use_llm")
        private Boolean useLlm;

        /**
         * 多模态LLM直接读图
         */
        @JsonProperty("llm_image")
        private Boolean llmImage;

        /**
         * 自动推断字段
         */
        @JsonProperty("auto_infer_fields")
        private Boolean autoInferFields;

        /**
         * 返回OCR诊断信息
         */
        @JsonProperty("analyze_ocr")
        private Boolean analyzeOcr;

        /**
         * 表格检测
         */
        @JsonProperty("detect_table")
        private Boolean detectTable;

        /**
         * 强制表格OCR
         */
        @JsonProperty("table_force")
        private Boolean tableForce;

        /**
         * 表格检测阈值
         */
        @JsonProperty("table_detect_threshold")
        private Double tableDetectThreshold;

        /**
         * LLM提供商（qwen/gemini/openai）
         */
        @JsonProperty("llm_provider")
        private String llmProvider;

        /**
         * LLM模型名称
         */
        @JsonProperty("llm_model")
        private String llmModel;
    }
}

