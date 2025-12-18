package cn.masu.dcs.controller;

import cn.masu.dcs.common.exception.BusinessException;
import cn.masu.dcs.common.result.ErrorCode;
import cn.masu.dcs.common.result.R;
import cn.masu.dcs.dto.AiProcessRequest;
import cn.masu.dcs.service.AiService;
import cn.masu.dcs.vo.AiProcessResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * AI智能处理控制器
 * <p>
 * 职责：
 * 1. 接收HTTP请求
 * 2. 参数校验
 * 3. 调用Service层业务逻辑
 * 4. 封装统一响应
 * </p>
 *
 * @author zyq
 * @since 2025-12-06
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    /**
     * 文件上传与智能解析
     *
     * @param file       上传的文件
     * @param templateId 模板ID
     * @return AI解析结果
     */
    @PostMapping("/upload-and-process")
    public R<AiProcessResponse> uploadAndProcess(
            @RequestParam("file") @NotNull(message = "文件不能为空") MultipartFile file,
            @RequestParam(value = "templateId", required = false) Long templateId) {

        Long userId = getCurrentUserIdOrThrow();
        AiProcessResponse response = aiService.uploadAndProcess(file, templateId, userId);
        return R.ok("文件上传并解析成功，请核对识别结果", response);
    }

    /**
     * 纯文本智能解析
     *
     * @param request AI处理请求
     * @return AI解析结果
     */
    @PostMapping("/process-text")
    public R<AiProcessResponse> processText(@Valid @RequestBody AiProcessRequest request) {
        AiProcessResponse response = aiService.processText(request);
        return R.ok("解析成功", response);
    }

    /**
     * 重新解析文件
     *
     * @param fileId     文件ID
     * @param templateId 模板ID
     * @return AI解析结果
     */
    @PostMapping("/reprocess/{fileId}")
    public R<AiProcessResponse> reprocess(
            @PathVariable("fileId") @NotNull(message = "文件ID不能为空") Long fileId,
            @RequestParam(value = "templateId", required = false) Long templateId) {

        AiProcessResponse response = aiService.reprocessFile(fileId, templateId);
        return R.ok("重新解析成功", response);
    }

    /**
     * 保存校对后的数据
     *
     * @param fileId   文件ID
     * @param response 校对后的数据
     * @return 保存结果
     */
    @PostMapping("/save/{fileId}")
    public R<Void> saveVerifiedData(
            @PathVariable("fileId") @NotNull(message = "文件ID不能为空") Long fileId,
            @Valid @RequestBody AiProcessResponse response) {

        aiService.saveVerifiedData(fileId, response);
        return R.ok("数据保存成功");
    }

    /**
     * 批量上传与解析
     *
     * @param files      文件列表
     * @param templateId 模板ID
     * @return 批量处理结果
     */
    @PostMapping("/batch-upload-and-process")
    public R<List<AiProcessResponse>> batchUploadAndProcess(
            @RequestParam("files") @NotNull(message = "文件列表不能为空") MultipartFile[] files,
            @RequestParam(value = "templateId", required = false) Long templateId) {

        Long userId = getCurrentUserIdOrThrow();
        List<AiProcessResponse> responses = aiService.batchUploadAndProcess(files, templateId, userId);

        long successCount = responses.stream().filter(r -> r.getSuccess() != null && r.getSuccess()).count();
        long failCount = responses.size() - successCount;
        String message = String.format("批量处理完成：成功 %d 个，失败 %d 个", successCount, failCount);

        return R.ok(message, responses);
    }

    /**
     * 获取当前登录用户ID
     *
     * @return 用户ID
     * @throws BusinessException 未登录时抛出异常
     */
    private Long getCurrentUserIdOrThrow() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            log.error("用户未登录");
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return userId;
    }

    /**
     * 从Security上下文获取用户ID
     *
     * @return 用户ID，未登录返回null
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof Long) {
            return (Long) principal;
        } else if (principal instanceof String) {
            try {
                return Long.parseLong((String) principal);
            } catch (NumberFormatException e) {
                log.warn("无法将principal转换为Long: principal={}", principal);
                return null;
            }
        }

        log.warn("不支持的principal类型: {}", principal.getClass().getName());
        return null;
    }
}
