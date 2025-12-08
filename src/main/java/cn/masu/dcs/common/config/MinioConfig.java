package cn.masu.dcs.common.config;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO配置类
 * <p>
 * 配置MinIO对象存储服务的连接参数
 * </p>
 * <p>
 * 配置项：
 * 1. endpoint - MinIO服务地址
 * 2. accessKey - 访问密钥
 * 3. secretKey - 秘密密钥
 * 4. bucketName - 默认存储桶名称
 * 5. urlPrefix - 文件访问URL前缀
 * </p>
 *
 * @author zyq
 * @since 2025-12-06
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinioConfig {

    /**
     * MinIO服务地址
     * <p>
     * 示例：http://localhost:9000
     * </p>
     */
    private String endpoint;

    /**
     * MinIO访问密钥（Access Key）
     * <p>
     * 用于身份验证
     * </p>
     */
    private String accessKey;

    /**
     * MinIO秘密密钥（Secret Key）
     * <p>
     * 用于身份验证
     * </p>
     */
    private String secretKey;

    /**
     * 默认存储桶名称
     * <p>
     * 示例：documents
     * </p>
     */
    private String bucketName;

    /**
     * 文件访问URL前缀
     * <p>
     * 用于拼接文件的完整访问路径
     * 示例：http://localhost:9000/documents/
     * </p>
     */
    private String urlPrefix;

    /**
     * 创建MinioClient Bean
     * <p>
     * 使用配置的endpoint、accessKey和secretKey创建MinIO客户端
     * </p>
     *
     * @return MinioClient实例
     */
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}

