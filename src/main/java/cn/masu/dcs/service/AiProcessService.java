package cn.masu.dcs.service;

import cn.masu.dcs.dto.AiProcessRequest;
import cn.masu.dcs.vo.AiProcessResponse;
import cn.masu.dcs.common.client.AIServiceClient;
import cn.masu.dcs.common.config.GlobalExceptionHandler;
import cn.masu.dcs.common.config.MinioConfig;
import cn.masu.dcs.common.result.ErrorCode;
import cn.masu.dcs.common.util.MinioUtils;
import cn.masu.dcs.common.util.SnowflakeIdGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Orchestrator for OCR + NLP pipeline aligned with /api/v1/ai/process.
 * @author zyq
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiProcessService {

    private final AIServiceClient aiServiceClient;
    private final SnowflakeIdGenerator idGenerator;
    private final ObjectMapper objectMapper;
    private final MinioUtils minioUtils;
    private final MinioConfig minioConfig;

    public AiProcessResponse process(AiProcessRequest request) {
        validateRequest(request);

        Long fileId = request.getFileId() != null ? request.getFileId() : idGenerator.nextId();
        request.setFileId(fileId);

        if (StringUtils.hasText(request.getFilePath())) {
            storeOriginalImage(request.getFilePath(), fileId);
        }

        try {
            AIServiceClient.AIProcessResponse aiResponse = aiServiceClient.processUnifiedAi(request);

            // 将JsonNode转换为AiProcessResponse
            AiProcessResponse response = objectMapper.treeToValue(aiResponse.getData(), AiProcessResponse.class);

            // 设置文件ID和处理时间
            if (response.getFileId() == null) {
                response.setFileId(fileId);
            }
            if (response.getProcessingTime() == null && aiResponse.getProcessingTime() != null) {
                response.setProcessingTime(aiResponse.getProcessingTime());
            }

            return response;
        } catch (Exception e) {
            log.error("AI process failed: fileId={}", fileId, e);
            throw new GlobalExceptionHandler.BusinessException(
                ErrorCode.SYSTEM_ERROR.getCode(),
                "AI process failed: " + e.getMessage()
            );
        }
    }

    private void validateRequest(AiProcessRequest request) {
        boolean hasFilePath = StringUtils.hasText(request.getFilePath());
        boolean hasText = StringUtils.hasText(request.getText());

        if (!hasFilePath && !hasText) {
            throw new GlobalExceptionHandler.BusinessException(
                ErrorCode.PARAM_ERROR.getCode(),
                "file_path or text is required"
            );
        }

        if (request.getTemplateConfig() == null || request.getTemplateConfig().getFields() == null) {
            throw new GlobalExceptionHandler.BusinessException(
                ErrorCode.PARAM_ERROR.getCode(),
                "template_config.fields is required"
            );
        }

        if (hasFilePath && !Files.exists(Paths.get(request.getFilePath()))) {
            throw new GlobalExceptionHandler.BusinessException(
                ErrorCode.PARAM_ERROR.getCode(),
                "file_path does not exist"
            );
        }
    }

    private void storeOriginalImage(String filePath, Long fileId) {
        if (!StringUtils.hasText(filePath)) {
            return;
        }

        try (InputStream inputStream = Files.newInputStream(Paths.get(filePath))) {
            long size = Files.size(Paths.get(filePath));
            String contentType = Files.probeContentType(Paths.get(filePath));
            if (!StringUtils.hasText(contentType)) {
                contentType = "application/octet-stream";
            }
            String fileName = Paths.get(filePath).getFileName().toString();
            String objectName = "ai/raw/" + fileId + "/" + fileName;
            minioUtils.uploadFile(minioConfig.getBucketName(), objectName, inputStream, size, contentType);
            log.info("Original image stored to MinIO: bucket={}, object={}", minioConfig.getBucketName(), objectName);
        } catch (Exception e) {
            log.error("Failed to store original image to MinIO: filePath={}, fileId={}", filePath, fileId, e);
            throw new GlobalExceptionHandler.BusinessException(
                ErrorCode.SYSTEM_ERROR.getCode(),
                "failed to store original image"
            );
        }
    }

}
