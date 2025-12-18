package cn.masu.dcs.service;

import cn.masu.dcs.common.config.MinioConfig;
import cn.masu.dcs.common.util.MinioUtils;
import cn.masu.dcs.common.util.SnowflakeIdGenerator;
import cn.masu.dcs.dto.AiDocProcessRequest;
import cn.masu.dcs.entity.DocumentFile;
import cn.masu.dcs.entity.SysUser;
import cn.masu.dcs.mapper.DocumentFileMapper;
import cn.masu.dcs.mapper.SysUserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * 负责将前端上传的Base64文件持久化到 MinIO 与数据库的服务，确保事务在代理中生效。
 * @author zyq
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiFileService {

    private final MinioUtils minioUtils;
    private final MinioConfig minioConfig;
    private final SnowflakeIdGenerator idGenerator;
    private final DocumentFileMapper fileMapper;
    private final SysUserMapper userMapper;

    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";
    private static final String CONTENT_TYPE_IMAGE_JPEG = "image/jpeg";
    private static final String CONTENT_TYPE_IMAGE_JPG = "image/jpg";
    private static final String CONTENT_TYPE_IMAGE_PNG = "image/png";
    private static final String EXT_PDF = "pdf";
    private static final String EXT_JPG = "jpg";
    private static final String EXT_PNG = "png";
    private static final String EXT_BIN = "bin";
    private static final String COMMA = ",";
    private static final String COLON = ":";
    private static final String SEMICOLON = ";";
    private static final String DOT = ".";

    @Transactional(rollbackFor = Exception.class)
    public Long saveFileToMinioAndDatabase(AiDocProcessRequest request, String requestId) throws Exception {
        String fileContent = request.getFileContent();
        String fileName = request.getFileName();

        String base64Data;
        String contentType = DEFAULT_CONTENT_TYPE;

        if (fileContent.contains(COMMA)) {
            String[] parts = fileContent.split(COMMA, 2);
            base64Data = parts[1];
            if (parts[0].contains(COLON) && parts[0].contains(SEMICOLON)) {
                contentType = parts[0].substring(parts[0].indexOf(COLON) + 1, parts[0].indexOf(SEMICOLON));
            }
        } else {
            base64Data = fileContent;
        }

        byte[] fileBytes = Base64.getDecoder().decode(base64Data);

        String fileExtension = resolveExtension(fileName, contentType);
        String bucketName = minioConfig.getBucketName();

        try (InputStream inputStream = new ByteArrayInputStream(fileBytes)) {
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String uuid = java.util.UUID.randomUUID().toString();
            String objectName = String.format("%s/%s.%s", datePath, uuid, fileExtension);

            minioUtils.uploadFile(bucketName, objectName, inputStream, fileBytes.length, contentType);
            log.info("文件上传到MinIO成功: bucket={}, object={}", bucketName, objectName);

            Long userId = getCurrentUserId();
            if (userId == null) {
                userId = getDefaultUserId();
                log.warn("未获取到当前用户ID，使用默认用户ID: {}", userId);
            }

            DocumentFile documentFile = new DocumentFile();
            documentFile.setId(idGenerator.nextId());
            documentFile.setFileName(fileName != null ? fileName : "document." + fileExtension);
            documentFile.setMinioBucket(bucketName);
            documentFile.setMinioObject(objectName);
            documentFile.setFileType(fileExtension);
            documentFile.setFileSize((long) fileBytes.length);
            documentFile.setUserId(userId);
            documentFile.setProcessStatus(1);
            documentFile.setProcessMode("AI_PROCESS");
            documentFile.setRetryCount(0);
            documentFile.setDeleted(0);
            documentFile.setVersion(0);
            if (StringUtils.hasText(requestId)) {
                documentFile.setBatchNo(requestId);
            }

            fileMapper.insert(documentFile);
            log.info("文件记录已保存到数据库: fileId={}", documentFile.getId());
            return documentFile.getId();
        }
    }

    private String resolveExtension(String fileName, String contentType) {
        if (fileName != null && fileName.contains(DOT)) {
            return fileName.substring(fileName.lastIndexOf(DOT) + 1).toLowerCase();
        }
        if (contentType.contains(EXT_PDF)) {
            return EXT_PDF;
        }
        if (contentType.contains(CONTENT_TYPE_IMAGE_JPEG) || contentType.contains(CONTENT_TYPE_IMAGE_JPG)) {
            return EXT_JPG;
        }
        if (contentType.contains(CONTENT_TYPE_IMAGE_PNG)) {
            return EXT_PNG;
        }
        return EXT_BIN;
    }

    private Long getCurrentUserId() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof java.util.Map<?, ?> userMap) {
                Object userIdObj = userMap.get("userId");
                if (userIdObj instanceof Long l) {
                    return l;
                } else if (userIdObj instanceof Integer i) {
                    return i.longValue();
                } else if (userIdObj != null) {
                    return Long.parseLong(userIdObj.toString());
                }
            }
        } catch (Exception e) {
            log.warn("获取当前用户ID失败: {}", e.getMessage());
        }
        return null;
    }

    private Long getDefaultUserId() {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SysUser::getId).last("LIMIT 1");
        SysUser user = userMapper.selectOne(wrapper);
        if (user != null) {
            log.info("使用数据库中的第一个用户: userId={}, username={}", user.getId(), user.getUsername());
            return user.getId();
        }
        throw new RuntimeException("无法获取有效用户ID，请先创建用户或登录系统");
    }
}
