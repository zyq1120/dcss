package cn.masu.dcs.service.impl;

import cn.masu.dcs.common.client.AIServiceClient;
import cn.masu.dcs.common.client.AIServiceClient.DocumentComplexity;
import cn.masu.dcs.common.client.AIServiceClient.DocumentProcessResult;
import cn.masu.dcs.common.exception.BusinessException;
import cn.masu.dcs.common.result.ErrorCode;
import cn.masu.dcs.entity.DocumentFile;
import cn.masu.dcs.mapper.DocumentFileMapper;
import cn.masu.dcs.entity.*;
import cn.masu.dcs.mapper.SysDocTemplateMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 文档智能处理服务
 * 展示如何使用AIServiceClient进行智能文档处理
 * @author System
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentIntelligentProcessService {

    private final AIServiceClient aiServiceClient;
    private final DocumentFileMapper fileMapper;
    private final SysDocTemplateMapper templateMapper;
    private final ObjectMapper objectMapper;

    // 文件大小阈值常量
    private static final long SIMPLE_FILE_SIZE_LIMIT = 500 * 1024;  // 500KB
    private static final long NORMAL_FILE_SIZE_LIMIT = 2 * 1024 * 1024;  // 2MB

    /**
     * 智能处理文档（推荐使用）
     * 自动选择最优处理策略（传统方法/LLM兜底/多模态LLM）
     */
    @Transactional(rollbackFor = Exception.class)
    public DocumentProcessResult smartProcessDocument(Long fileId) {
        log.info("开始智能处理文档: fileId={}", fileId);

        // 1. 获取文件信息
        DocumentFile file = fileMapper.selectById(fileId);
        if (file == null) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }

        // 2. 获取模板配置
        Map<String, Object> templateConfig = getTemplateConfig(file.getTemplateId());

        // 3. 判断文档复杂度
        DocumentComplexity complexity = determineComplexity(file);
        log.info("文档复杂度评估: fileId={}, complexity={}", fileId, complexity);

        // 4. 智能处理
        try {
            // 构建MinIO文件路径
            String filePath = buildMinioFilePath(file);

            DocumentProcessResult result = aiServiceClient.smartProcessDocument(
                filePath,
                fileId,
                templateConfig,
                complexity
            );

            if (result.getSuccess()) {
                log.info("文档处理成功: fileId={}, strategy={}", fileId, result.getStrategy());

                // 更新文件状态
                updateFileStatus(file, result);

                // 打印成本统计
                logCostStatistics();
            } else {
                log.error("文档处理失败: fileId={}, error={}", fileId, result.getErrorMessage());
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, result.getErrorMessage());
            }

            return result;

        } catch (Exception e) {
            log.error("智能处理文档异常: fileId={}", fileId, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文档处理失败");
        }
    }

    /**
     * 批量处理文档
     */
    public void batchProcessDocuments(java.util.List<Long> fileIds) {
        log.info("开始批量处理文档: count={}", fileIds.size());

        int successCount = 0;
        int failCount = 0;

        for (Long fileId : fileIds) {
            try {
                smartProcessDocument(fileId);
                successCount++;
            } catch (Exception e) {
                log.error("处理文档失败: fileId={}", fileId, e);
                failCount++;
            }
        }

        log.info("批量处理完成: total={}, success={}, fail={}",
            fileIds.size(), successCount, failCount);

        // 打印成本统计
        logCostStatistics();
    }

    /**
     * 判断文档复杂度
     */
    private DocumentComplexity determineComplexity(DocumentFile file) {
        // 策略1: 根据文件大小判断
        long fileSize = file.getFileSize();

        if (fileSize < SIMPLE_FILE_SIZE_LIMIT) {
            return DocumentComplexity.SIMPLE;
        } else if (fileSize < NORMAL_FILE_SIZE_LIMIT) {
            return DocumentComplexity.NORMAL;
        } else {
            return DocumentComplexity.COMPLEX;
        }
    }

    /**
     * 获取模板配置
     */
    private Map<String, Object> getTemplateConfig(Long templateId) {
        SysDocTemplate template = templateMapper.selectById(templateId);

        Map<String, Object> config = new HashMap<>();

        if (template == null) {
            log.warn("模板不存在: templateId={}, 使用默认配置", templateId);
            config.put("fields", getDefaultFields());
            return config;
        }

        // 根据模板实际字段解析配置
        try {
            config.put("templateCode", template.getTemplateCode());
            config.put("templateName", template.getTemplateName());

            // 解析KV配置（JSON格式）
            if (template.getTargetKvConfig() != null && !template.getTargetKvConfig().isEmpty()) {
                config.put("kvFields", parseJsonConfig(template.getTargetKvConfig()));
            } else {
                config.put("kvFields", getDefaultFields());
            }

            // 解析表格配置（JSON格式）
            if (template.getTargetTableConfig() != null && !template.getTargetTableConfig().isEmpty()) {
                config.put("tableFields", parseJsonConfig(template.getTargetTableConfig()));
            }

            // 解析规则配置（JSON格式）
            if (template.getRuleConfig() != null && !template.getRuleConfig().isEmpty()) {
                config.put("rules", parseJsonConfig(template.getRuleConfig()));
            }

            log.info("模板配置解析成功: templateId={}, templateCode={}",
                templateId, template.getTemplateCode());

        } catch (Exception e) {
            log.error("模板配置解析失败，使用默认配置: templateId={}", templateId, e);
            config.put("fields", getDefaultFields());
        }

        return config;
    }

    /**
     * 解析JSON配置
     */
    private Object parseJsonConfig(String jsonConfig) {
        try {
            return objectMapper.readValue(jsonConfig, Object.class);
        } catch (Exception e) {
            log.warn("JSON配置解析失败: {}", jsonConfig, e);
            return null;
        }
    }

    /**
     * 获取默认字段定义
     */
    private java.util.List<Map<String, Object>> getDefaultFields() {
        java.util.List<Map<String, Object>> fields = new java.util.ArrayList<>();

        // 学生姓名
        Map<String, Object> nameField = new HashMap<>();
        nameField.put("name", "student_name");
        nameField.put("type", "PERSON");
        nameField.put("required", true);
        nameField.put("description", "学生姓名");
        fields.add(nameField);

        // 学号
        Map<String, Object> idField = new HashMap<>();
        idField.put("name", "student_id");
        idField.put("type", "NUMBER");
        idField.put("required", true);
        idField.put("description", "10位学号");
        fields.add(idField);

        // 课程成绩
        Map<String, Object> coursesField = new HashMap<>();
        coursesField.put("name", "courses");
        coursesField.put("type", "ARRAY");
        coursesField.put("required", false);
        coursesField.put("description", "课程成绩列表");
        fields.add(coursesField);

        return fields;
    }

    /**
     * 构建MinIO文件路径
     */
    private String buildMinioFilePath(DocumentFile file) {
        // 格式: bucket/object
        return file.getMinioBucket() + "/" + file.getMinioObject();
    }

    /**
     * 更新文件状态
     */
    private void updateFileStatus(DocumentFile file, DocumentProcessResult result) {
        final int statusProcessing = 2;
        final int statusManual = 3;
        final int statusArchived = 4;
        final double confidenceThreshold = 0.85;

        // 根据处理结果更新文件状态
        if (result.getOcrResponse() != null && result.getNlpResponse() != null) {
            // 检查OCR置信度
            Double ocrConfidence = result.getOcrResponse().getConfidence();
            if (ocrConfidence != null && ocrConfidence > confidenceThreshold) {
                file.setProcessStatus(statusArchived);
            } else {
                file.setProcessStatus(statusManual);
            }
        } else {
            file.setProcessStatus(statusProcessing);
        }

        fileMapper.updateById(file);
    }

    /**
     * 打印成本统计
     */
    private void logCostStatistics() {
        AIServiceClient.CostStatistics stats = aiServiceClient.getCostStatistics();
        log.info("=== AI服务成本统计 ===");
        log.info("当前成本: ¥{}", stats.getCurrentCost());
        log.info("每日预算: ¥{}", stats.getDailyBudget());
        log.info("剩余预算: ¥{}", stats.getRemainingBudget());
        log.info("使用率: {}%", String.format("%.2f", stats.getUsagePercentage()));
        log.info("=====================");
    }

    /**
     * 重置每日成本（定时任务调用）
     */
    public void resetDailyCost() {
        aiServiceClient.resetDailyCost();
        log.info("每日成本已重置");
    }
}

