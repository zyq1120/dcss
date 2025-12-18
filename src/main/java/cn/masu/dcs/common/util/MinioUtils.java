package cn.masu.dcs.common.util;

import cn.masu.dcs.common.exception.BusinessException;
import cn.masu.dcs.common.result.ErrorCode;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * MinIO工具类
 * <p>
 * 提供MinIO对象存储的常用操作：
 * 1. 存储桶管理（创建、检查、列表）
 * 2. 文件上传（支持MultipartFile和InputStream）
 * 3. 文件下载
 * 4. 文件删除
 * 5. 获取文件访问URL（临时签名URL）
 * 6. 文件存在性检查
 * </p>
 *
 * @author zyq
 * @since 2025-12-06
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MinioUtils {

    private final MinioClient minioClient;

    /**
     * 默认临时URL有效期（天）
     */
    private static final int DEFAULT_EXPIRY_DAYS = 7;

    /**
     * 日期格式化器
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    /**
     * 文件名分隔符
     */
    private static final String FILE_EXTENSION_SEPARATOR = ".";

    /**
     * 检查存储桶是否存在
     *
     * @param bucketName 存储桶名称
     * @return true-存在，false-不存在
     */
    public boolean bucketExists(String bucketName) {
        validateBucketName(bucketName);

        try {
            return minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());
        } catch (Exception e) {
            log.error("检查存储桶是否存在失败: bucketName={}", bucketName, e);
            return false;
        }
    }

    /**
     * 创建存储桶
     *
     * @param bucketName 存储桶名称
     */
    public void createBucket(String bucketName) {
        validateBucketName(bucketName);

        try {
            if (!bucketExists(bucketName)) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
                log.info("创建存储桶成功: bucketName={}", bucketName);
            } else {
                log.debug("存储桶已存在: bucketName={}", bucketName);
            }
        } catch (Exception e) {
            log.error("创建存储桶失败: bucketName={}", bucketName, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(),
                    "创建存储桶失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有存储桶
     *
     * @return 存储桶列表
     */
    public List<Bucket> getAllBuckets() {
        try {
            return minioClient.listBuckets();
        } catch (Exception e) {
            log.error("获取所有存储桶失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(),
                    "获取存储桶列表失败: " + e.getMessage());
        }
    }

    /**
     * 上传文件
     * <p>
     * 文件路径格式：{yyyy/MM/dd}/{uuid}.{extension}
     * 例如：2025/12/06/a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg
     * </p>
     *
     * @param file       文件
     * @param bucketName 存储桶名称
     * @return 文件对象名称（相对路径）
     */
    public String uploadFile(MultipartFile file, String bucketName) {
        // 参数校验
        validateBucketName(bucketName);
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "文件不能为空");
        }

        try {
            // 确保存储桶存在
            createBucket(bucketName);

            // 生成文件路径: 年/月/日/uuid.扩展名
            String objectName = generateObjectName(file.getOriginalFilename());

            // 上传文件
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());

            log.info("文件上传成功: bucketName={}, objectName={}, size={}",
                    bucketName, objectName, file.getSize());
            return objectName;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("文件上传失败: bucketName={}, fileName={}",
                    bucketName, file.getOriginalFilename(), e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR.getCode(),
                    "文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传文件（指定对象名称）
     *
     * @param bucketName  存储桶名称
     * @param objectName  对象名称
     * @param inputStream 输入流
     * @param size        文件大小
     * @param contentType 内容类型
     */
    public void uploadFile(String bucketName, String objectName, InputStream inputStream,
                          long size, String contentType) {
        // 参数校验
        validateBucketName(bucketName);
        validateObjectName(objectName);
        if (inputStream == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "输入流不能为空");
        }
        if (size <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "文件大小必须大于0");
        }

        try {
            createBucket(bucketName);

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, size, -1)
                    .contentType(contentType != null ? contentType : "application/octet-stream")
                    .build());

            log.info("文件上传成功: bucketName={}, objectName={}, size={}",
                    bucketName, objectName, size);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("文件上传失败: bucketName={}, objectName={}", bucketName, objectName, e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR.getCode(),
                    "文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 下载文件
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return 文件输入流
     */
    public InputStream downloadFile(String bucketName, String objectName) {
        validateBucketName(bucketName);
        validateObjectName(objectName);

        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            log.error("文件下载失败: bucketName={}, objectName={}", bucketName, objectName, e);
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND.getCode(),
                    "文件下载失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     */
    public void deleteFile(String bucketName, String objectName) {
        validateBucketName(bucketName);
        validateObjectName(objectName);

        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());

            log.info("文件删除成功: bucketName={}, objectName={}", bucketName, objectName);

        } catch (Exception e) {
            log.error("文件删除失败: bucketName={}, objectName={}", bucketName, objectName, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(),
                    "文件删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件访问URL（临时URL，默认有效期7天）
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return 预签名URL
     */
    public String getPresignedObjectUrl(String bucketName, String objectName) {
        return getPresignedObjectUrl(bucketName, objectName, DEFAULT_EXPIRY_DAYS, TimeUnit.DAYS);
    }

    /**
     * 获取文件访问URL（指定有效期）
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @param expiry     有效期时长
     * @param timeUnit   时间单位
     * @return 预签名URL
     */
    public String getPresignedObjectUrl(String bucketName, String objectName,
                                       int expiry, TimeUnit timeUnit) {
        validateBucketName(bucketName);
        validateObjectName(objectName);
        if (expiry <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "有效期必须大于0");
        }
        if (timeUnit == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "时间单位不能为空");
        }

        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(objectName)
                    .expiry(expiry, timeUnit)
                    .build());
        } catch (Exception e) {
            log.error("获取文件访问URL失败: bucketName={}, objectName={}", bucketName, objectName, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(),
                    "获取文件访问URL失败: " + e.getMessage());
        }
    }

    /**
     * 检查文件是否存在
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return true-存在，false-不存在
     */
    public boolean objectExists(String bucketName, String objectName) {
        validateBucketName(bucketName);
        validateObjectName(objectName);

        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
            return true;
        } catch (Exception e) {
            log.debug("文件不存在或访问失败: bucketName={}, objectName={}", bucketName, objectName);
            return false;
        }
    }

    /**
     * 生成对象名称
     * <p>
     * 格式：{yyyy/MM/dd}/{uuid}.{extension}
     * </p>
     *
     * @param originalFilename 原始文件名
     * @return 对象名称
     */
    private String generateObjectName(String originalFilename) {
        // 提取文件扩展名
        String extension = "";
        if (StringUtils.hasText(originalFilename) && originalFilename.contains(FILE_EXTENSION_SEPARATOR)) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(FILE_EXTENSION_SEPARATOR));
        }

        // 生成路径：年/月/日/UUID.扩展名
        String datePath = LocalDate.now().format(DATE_FORMATTER);
        String fileName = UUID.randomUUID() + extension;

        return datePath + "/" + fileName;
    }

    /**
     * 校验存储桶名称
     *
     * @param bucketName 存储桶名称
     */
    private void validateBucketName(String bucketName) {
        if (!StringUtils.hasText(bucketName)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "存储桶名称不能为空");
        }
    }

    /**
     * 校验对象名称
     *
     * @param objectName 对象名称
     */
    private void validateObjectName(String objectName) {
        if (!StringUtils.hasText(objectName)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "对象名称不能为空");
        }
    }
}
