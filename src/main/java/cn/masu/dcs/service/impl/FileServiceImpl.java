package cn.masu.dcs.service.impl;

import cn.masu.dcs.common.config.GlobalExceptionHandler.BusinessException;
import cn.masu.dcs.common.config.MinioConfig;
import cn.masu.dcs.common.result.ErrorCode;
import cn.masu.dcs.common.result.PageResult;
import cn.masu.dcs.common.util.MinioUtils;
import cn.masu.dcs.common.util.SnowflakeIdGenerator;
import cn.masu.dcs.dto.FileUpdateStatusDTO;
import cn.masu.dcs.entity.DocumentFile;
import cn.masu.dcs.mapper.DocumentFileMapper;
import cn.masu.dcs.service.FileService;
import cn.masu.dcs.vo.FileDetailVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

/**
 * File service implementation.
 * @author zyq
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl extends ServiceImpl<DocumentFileMapper, DocumentFile> implements FileService {

    private final SnowflakeIdGenerator idGenerator;
    private final MinioUtils minioUtils;
    private final MinioConfig minioConfig;

    @Override
    public Long uploadFile(MultipartFile file, Long templateId, Long userId) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR.getCode(), "File cannot be empty");
        }

        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        if (!isValidFileType(fileExtension)) {
            throw new BusinessException(ErrorCode.FILE_TYPE_ERROR);
        }

        // 10 MB limit
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new BusinessException(ErrorCode.FILE_SIZE_ERROR);
        }

        try {
            String bucketName = minioConfig.getBucketName();
            String objectName = minioUtils.uploadFile(file, bucketName);

            DocumentFile documentFile = new DocumentFile();
            documentFile.setId(idGenerator.nextId());
            documentFile.setFileName(originalFilename);
            documentFile.setMinioBucket(bucketName);
            documentFile.setMinioObject(objectName);
            documentFile.setFileType(fileExtension);
            documentFile.setFileSize(file.getSize());
            documentFile.setUserId(userId);
            documentFile.setTemplateId(templateId);
            documentFile.setProcessStatus(0);
            documentFile.setRetryCount(0);
            documentFile.setDeleted(0);
            documentFile.setVersion(0);

            baseMapper.insert(documentFile);

            log.info("File uploaded: fileName={}, fileId={}, objectName={}",
                originalFilename, documentFile.getId(), objectName);
            return documentFile.getId();

        } catch (Exception e) {
            log.error("File upload failed", e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR);
        }
    }

    @Override
    public byte[] downloadFile(Long id) {
        DocumentFile file = baseMapper.selectById(id);
        if (file == null) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }

        try {
            String bucketName = file.getMinioBucket();    // ✅ 修复：使用minioBucket
            String objectName = file.getMinioObject();    // ✅ 修复：使用minioObject

            try (InputStream inputStream = minioUtils.downloadFile(bucketName, objectName);
                 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                return outputStream.toByteArray();
            }

        } catch (Exception e) {
            log.error("File download failed: fileId={}", id, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "File download failed");
        }
    }

    @Override
    public Boolean deleteFile(Long id) {
        DocumentFile file = baseMapper.selectById(id);
        if (file == null) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }

        try {
            String bucketName = file.getMinioBucket();    // ✅ 修复：使用minioBucket
            String objectName = file.getMinioObject();    // ✅ 修复：使用minioObject
            minioUtils.deleteFile(bucketName, objectName);
            log.info("MinIO file removed: fileId={}, objectName={}", id, objectName);
        } catch (Exception e) {
            log.error("MinIO delete failed: fileId={}", id, e);
        }

        return baseMapper.deleteById(id) > 0;
    }

    @Override
    public FileDetailVO getFileDetail(Long id) {
        DocumentFile file = baseMapper.selectById(id);
        if (file == null) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }
        return convertToVO(file);
    }

    @Override
    public Boolean updateFileStatus(FileUpdateStatusDTO dto) {
        DocumentFile file = baseMapper.selectById(dto.getId());
        if (file == null) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }

        // ✅ 修复：使用processStatus代替多个status字段
        if (dto.getStatus() != null) {
            file.setProcessStatus(dto.getStatus());
        }
        // 如果DTO有其他状态字段，也可以映射到processStatus或failReason
        if (dto.getOcrStatus() != null || dto.getNlpStatus() != null || dto.getAuditStatus() != null) {
            // 这些字段已废弃，使用processStatus统一管理
            log.warn("使用了已废弃的状态字段，建议使用processStatus");
        }

        return baseMapper.updateById(file) > 0;
    }

    @Override
    public PageResult<FileDetailVO> getFilePage(Long current, Long size, String keyword, Integer status, Long userId) {
        Page<DocumentFile> page = new Page<>(current, size);
        LambdaQueryWrapper<DocumentFile> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.like(DocumentFile::getFileName, keyword);
        }

        if (status != null) {
            wrapper.eq(DocumentFile::getProcessStatus, status);  // ✅ 修复：使用processStatus
        }

        if (userId != null) {
            wrapper.eq(DocumentFile::getUserId, userId);         // ✅ 修复：使用userId
        }

        wrapper.orderByDesc(DocumentFile::getCreateTime);

        Page<DocumentFile> result = baseMapper.selectPage(page, wrapper);

        List<FileDetailVO> voList = result.getRecords().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());

        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), voList);
    }

    @Override
    public Boolean updateFileTemplate(Long fileId, Long templateId) {
        DocumentFile file = baseMapper.selectById(fileId);
        if (file == null) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }

        file.setTemplateId(templateId);
        int result = baseMapper.updateById(file);

        log.info("文件模板更新: fileId={}, templateId={}", fileId, templateId);
        return result > 0;
    }

    private FileDetailVO convertToVO(DocumentFile file) {
        FileDetailVO vo = new FileDetailVO();
        BeanUtils.copyProperties(file, vo);

        // ✅ 修复：使用processStatus代替多个status字段
        vo.setStatusName(getProcessStatusName(file.getProcessStatus()));
        // 旧的OCR/NLP/Audit状态字段已废弃，不再需要
        return vo;
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private boolean isValidFileType(String extension) {
        return "pdf".equals(extension) || "jpg".equals(extension) ||
            "jpeg".equals(extension) || "png".equals(extension);
    }

    /**
     * 获取处理状态名称
     * 0=pending, 1=queued, 2=processing, 3=manual, 4=archived, 5=failed
     */
    private String getProcessStatusName(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "待处理";
            case 1 -> "已入队";
            case 2 -> "处理中";
            case 3 -> "待人工";
            case 4 -> "已归档";
            case 5 -> "失败";
            default -> "未知";
        };
    }
}
