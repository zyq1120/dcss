package cn.masu.dcs.service.impl;

import cn.masu.dcs.common.client.AIServiceClient;
import cn.masu.dcs.common.exception.BusinessException;
import cn.masu.dcs.common.result.ErrorCode;
import cn.masu.dcs.common.util.MinioUtils;
import cn.masu.dcs.dto.AiDocProcessRequest;
import cn.masu.dcs.dto.AiProcessOptions;
import cn.masu.dcs.entity.DocumentFile;
import cn.masu.dcs.mapper.DocumentFileMapper;
import cn.masu.dcs.service.AiFileService;
import cn.masu.dcs.service.AiProcessorService;
import cn.masu.dcs.service.AiResultPersistenceService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * AI文档处理服务实现
 *
 * @author zyq
 * @since 2025-12-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiProcessorServiceImpl implements AiProcessorService {

    private final AIServiceClient aiServiceClient;
    private final ObjectMapper objectMapper;
    private final MinioUtils minioUtils;
    private final DocumentFileMapper fileMapper;
    private final AiFileService aiFileService;

    /**
     * AI结果持久化服务（独立事务服务，解决事务自调用问题）
     */
    private final AiResultPersistenceService aiResultPersistenceService;

    private static final String DOT = ".";
    private static final int DEFAULT_MAP_CAPACITY = 8;
    private static final int FILE_INFO_MAP_CAPACITY = 4;

    @Override
    public Map<String, Object> processDocument(AiDocProcessRequest request) {
        validateRequest(request);

        Long fileId = request.getFileId();
        String bucketName = null;
        String objectName = null;
        String fileUrl;
        boolean newFileCreated = false;
        Path tempFilePath = null;

        try {
            // 处理文件
            FileProcessResult fileResult = handleFileInput(request);
            fileId = fileResult.fileId;
            bucketName = fileResult.bucketName;
            objectName = fileResult.objectName;
            fileUrl = fileResult.fileUrl;
            newFileCreated = fileResult.newFileCreated;

            // 确定文件名（优先使用数据库中的文件名，其次使用请求中的文件名）
            String effectiveFileName = fileResult.fileName != null ? fileResult.fileName : request.getFileName();

            // 下载到临时路径供Python使用
            if (bucketName != null && objectName != null) {
                tempFilePath = downloadFileToTemp(bucketName, objectName, effectiveFileName);
            }

            // 调用AI服务
            JsonNode aiResult = callAiService(request, fileId, bucketName, objectName, fileUrl, tempFilePath);

            // 保存结果到数据库（通过独立的事务服务调用，确保事务生效）
            if (fileId != null) {
                aiResultPersistenceService.saveAiResult(fileId, aiResult, fileUrl);
            }

            // 组装返回结果
            return buildSuccessResult(fileId, aiResult, bucketName, objectName, fileUrl);

        } catch (Exception e) {
            log.error("AI处理失败", e);
            if (newFileCreated && fileId != null) {
                cleanupUploadedFile(fileId, bucketName, objectName);
            }
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "AI处理失败: " + e.getMessage());
        } finally {
            deleteTempFile(tempFilePath);
        }
    }

    @Override
    public Map<String, Object> processFile(MultipartFile file, String optionsJson, String templateConfigJson) {
        try {
            AiDocProcessRequest request = convertMultipartToRequest(file, optionsJson, templateConfigJson);
            return processDocument(request);
        } catch (IOException e) {
            log.error("文件处理失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "文件处理失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getServiceStatus() {
        Map<String, Object> status = new HashMap<>(DEFAULT_MAP_CAPACITY);
        status.put("service", "AI Document Assistant");
        status.put("status", "ready");
        status.put("version", "1.0.0");
        status.put("backend", "Python AI Service");

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

        return status;
    }

    /**
     * 校验请求参数
     */
    private void validateRequest(AiDocProcessRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "请求体不能为空");
        }

        boolean hasFileId = request.getFileId() != null && request.getFileId() > 0;
        boolean hasFileContent = StringUtils.hasText(request.getFileContent());
        boolean hasText = StringUtils.hasText(request.getText());

        if (!hasFileId && !hasFileContent && !hasText) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(),
                "必须提供fileId、fileContent或text之一");
        }
    }

    /**
     * 处理文件输入
     */
    private FileProcessResult handleFileInput(AiDocProcessRequest request) throws Exception {
        FileProcessResult result = new FileProcessResult();
        Long requestFileId = request.getFileId();
        String fileContent = request.getFileContent();

        boolean hasFileId = requestFileId != null && requestFileId > 0;
        boolean hasFileContent = StringUtils.hasText(fileContent);

        if (hasFileId) {
            DocumentFile existingFile = fileMapper.selectById(requestFileId);
            if (existingFile == null) {
                throw new BusinessException(ErrorCode.FILE_NOT_FOUND.getCode(),
                    "文件不存在: fileId=" + requestFileId);
            }
            result.fileId = requestFileId;
            result.bucketName = existingFile.getMinioBucket();
            result.objectName = existingFile.getMinioObject();
            result.fileName = existingFile.getFileName();
            result.fileUrl = buildMinioUrl(result.bucketName, result.objectName);
        } else if (hasFileContent) {
            result.fileId = aiFileService.saveFileToMinioAndDatabase(request, request.getRequestId());
            result.newFileCreated = true;

            DocumentFile savedFile = fileMapper.selectById(result.fileId);
            if (savedFile == null) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "文件保存异常");
            }
            result.bucketName = savedFile.getMinioBucket();
            result.objectName = savedFile.getMinioObject();
            result.fileName = savedFile.getFileName();
            result.fileUrl = buildMinioUrl(result.bucketName, result.objectName);
        }

        return result;
    }

    /**
     * 调用AI服务
     */
    private JsonNode callAiService(AiDocProcessRequest request, Long fileId,
                                   String bucketName, String objectName, String fileUrl,
                                   Path tempFilePath) {
        Map<String, Object> pythonRequest = buildPythonRequest(request, fileId, bucketName,
            objectName, fileUrl, tempFilePath != null ? tempFilePath.toString() : null);

        AIServiceClient.AIProcessResponse aiResponse = aiServiceClient.processUnifiedAi(pythonRequest);

        if (aiResponse == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "AI服务无返回结g果");
        }
        if (Boolean.FALSE.equals(aiResponse.getSuccess())) {
            String errorMsg = aiResponse.getErrorMessage() != null ? aiResponse.getErrorMessage() : "未知错误";
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "AI处理失败: " + errorMsg);
        }

        return aiResponse.getData();
    }

    /**
     * 构建Python请求
     */
    private Map<String, Object> buildPythonRequest(AiDocProcessRequest request, Long fileId,
                                                   String bucketName, String objectName,
                                                   String fileUrl, String localFilePath) {
        Map<String, Object> pythonRequest = new HashMap<>(DEFAULT_MAP_CAPACITY);

        if (StringUtils.hasText(request.getText())) {
            pythonRequest.put("text", request.getText().trim());
        }
        if (StringUtils.hasText(request.getFileContent())) {
            pythonRequest.put("file_content", request.getFileContent().trim());
        }
        if (request.getOptions() != null) {
            pythonRequest.put("options", request.getOptions());
        }
        if (fileId != null || bucketName != null) {
            Map<String, Object> fileMeta = new HashMap<>(FILE_INFO_MAP_CAPACITY);
            fileMeta.put("file_id", fileId);
            fileMeta.put("bucket", bucketName);
            fileMeta.put("object", objectName);
            fileMeta.put("url", fileUrl);
            pythonRequest.put("file_meta", fileMeta);
        }
        if (localFilePath != null) {
            pythonRequest.put("file_path", localFilePath);
        }

        return pythonRequest;
    }

    /**
     * 保存AI处理结果（委托给独立事务服务）
     * <p>
     * 此方法保留为兼容调用，实际事务由 AiResultPersistenceService 处理
     * </p>
     *
     * @param fileId   文件ID
     * @param aiResult AI处理结果
     * @param fileUrl  文件URL
     * @deprecated 请直接使用 aiResultPersistenceService.saveAiResult()
     */
    @Deprecated
    @SuppressWarnings("unused")
    public void saveAiResult(Long fileId, JsonNode aiResult, String fileUrl) {
        aiResultPersistenceService.saveAiResult(fileId, aiResult, fileUrl);
    }


    /**
     * 构建成功结果
     */
    private Map<String, Object> buildSuccessResult(Long fileId, JsonNode aiResult,
                                                   String bucketName, String objectName, String fileUrl) {
        Map<String, Object> result = new HashMap<>(DEFAULT_MAP_CAPACITY);
        result.put("fileId", fileId);
        result.put("aiResult", aiResult);

        if (bucketName != null && objectName != null) {
            Map<String, Object> fileInfo = new HashMap<>(FILE_INFO_MAP_CAPACITY);
            fileInfo.put("bucket", bucketName);
            fileInfo.put("object", objectName);
            fileInfo.put("url", fileUrl);
            fileInfo.put("fileId", fileId);
            result.put("fileInfo", fileInfo);
        }

        return result;
    }

    /**
     * 转换MultipartFile为请求对�?
     */
    private AiDocProcessRequest convertMultipartToRequest(MultipartFile file,
                                                                  String optionsJson,
                                                                  String templateConfigJson) throws IOException {
        byte[] bytes = file.getBytes();
        String base64 = Base64.getEncoder().encodeToString(bytes);
        String fileContent = "data:" + file.getContentType() + ";base64," + base64;

        AiDocProcessRequest request = new AiDocProcessRequest();
        request.setFileContent(fileContent);
        request.setFileName(file.getOriginalFilename());

        if (StringUtils.hasText(optionsJson)) {
            request.setOptions(objectMapper.readValue(optionsJson, AiProcessOptions.class));
        }
        if (StringUtils.hasText(templateConfigJson)) {
            @SuppressWarnings("unchecked")
            Map<String, Object> templateConfig = objectMapper.readValue(templateConfigJson, Map.class);
            request.setTemplateConfig(templateConfig);
        }

        return request;
    }

    /**
     * 下载文件到临时目录
     */
    private Path downloadFileToTemp(String bucketName, String objectName, String originalFileName) {
        try (InputStream inputStream = minioUtils.downloadFile(bucketName, objectName)) {
            String suffix = "";

            // 优先从原始文件名获取扩展名
            if (StringUtils.hasText(originalFileName) && originalFileName.contains(DOT)) {
                suffix = originalFileName.substring(originalFileName.lastIndexOf(DOT));
            }
            // 如果文件名为空，从objectName获取扩展名
            else if (StringUtils.hasText(objectName) && objectName.contains(DOT)) {
                suffix = objectName.substring(objectName.lastIndexOf(DOT));
            }

            Path tempFile = Files.createTempFile("ai-process-", suffix);
            Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
            log.debug("文件下载到临时路径: {} (suffix={})", tempFile, suffix);
            return tempFile;
        } catch (Exception e) {
            log.error("下载文件到临时目录失败: {}/{}", bucketName, objectName, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "下载文件失败");
        }
    }

    /**
     * 构建MinIO访问URL
     */
    private String buildMinioUrl(String bucketName, String objectName) {
        try {
            return minioUtils.getPresignedObjectUrl(bucketName, objectName);
        } catch (Exception e) {
            log.warn("生成MinIO访问链接失败: {}/{}", bucketName, objectName, e);
            return String.format("minio://%s/%s", bucketName, objectName);
        }
    }

    /**
     * 清理上传的文件
     */
    private void cleanupUploadedFile(Long fileId, String bucketName, String objectName) {
        try {
            if (bucketName != null && objectName != null) {
                minioUtils.deleteFile(bucketName, objectName);
            }
            if (fileId != null) {
                fileMapper.deleteById(fileId);
            }
        } catch (Exception e) {
            log.error("清理上传文件失败: fileId={}", fileId, e);
        }
    }

    /**
     * 删除临时文件
     */
    private void deleteTempFile(Path tempFilePath) {
        if (tempFilePath != null) {
            try {
                Files.deleteIfExists(tempFilePath);
            } catch (Exception e) {
                log.warn("删除临时文件失败: {}", tempFilePath, e);
            }
        }
    }

    /**
     * 文件处理结果内部类
     */
    private static class FileProcessResult {
        Long fileId;
        String bucketName;
        String objectName;
        String fileUrl;
        String fileName;
        boolean newFileCreated;
    }
}
