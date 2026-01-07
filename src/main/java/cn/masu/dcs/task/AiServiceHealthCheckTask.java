package cn.masu.dcs.task;

import cn.masu.dcs.common.config.AiServiceProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AI服务健康检查定时任务
 * <p>
 * 功能：
 * 1. 应用启动时轮询检查Python AI服务是否可用
 * 2. 定时检查AI服务健康状态
 * 3. 记录服务连接状态变化
 * </p>
 *
 * @author zyq
 * @since 2025-12-19
 */
@Slf4j
@Component
@EnableScheduling
@EnableAsync
@RequiredArgsConstructor
public class AiServiceHealthCheckTask {

    private final AiServiceProperties aiServiceProperties;
    private final ObjectMapper objectMapper;

    /**
     * AI服务是否可用
     */
    private final AtomicBoolean aiServiceAvailable = new AtomicBoolean(false);

    /**
     * 连续失败次数
     */
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);

    /**
     * 启动时最大重试次数
     */
    private static final int MAX_STARTUP_RETRIES = 30;

    /**
     * 启动时重试间隔（毫秒）
     */
    private static final long STARTUP_RETRY_INTERVAL_MS = 2000;

    /**
     * 健康检查超时时间（秒）
     */
    private static final int HEALTH_CHECK_TIMEOUT_SECONDS = 10;

    /**
     * 连续失败告警阈值
     */
    private static final int FAILURE_ALERT_THRESHOLD = 3;

    /**
     * JSON字段名常量
     */
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_SERVICE = "service";
    private static final String FIELD_VERSION = "version";
    private static final String FIELD_MESSAGE = "message";

    private final OkHttpClient healthCheckClient = new OkHttpClient.Builder()
            .connectTimeout(HEALTH_CHECK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(HEALTH_CHECK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(HEALTH_CHECK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .callTimeout(Duration.ofSeconds(HEALTH_CHECK_TIMEOUT_SECONDS))
            .build();

    /**
     * 应用启动完成后，轮询检查AI服务直到连接成功
     */
    @EventListener(ApplicationReadyEvent.class)
    @Async
    public void onApplicationReady() {
        log.info("========== 应用启动完成，开始检查AI服务连接状态 ==========");
        log.info("AI服务地址: {}", aiServiceProperties.getBaseUrl());

        boolean connected = checkAiServiceWithRetry();

        if (connected) {
            aiServiceAvailable.set(true);
            consecutiveFailures.set(0);
            log.info("========== AI服务连接成功！==========");
        } else {
            log.error("========== AI服务连接失败！已重试{}次 ==========", MAX_STARTUP_RETRIES);
            log.error("请检查AI服务是否启动，地址: {}", aiServiceProperties.getBaseUrl());
        }
    }

    /**
     * 带重试的AI服务检查
     *
     * @return 是否连接成功
     */
    private boolean checkAiServiceWithRetry() {
        for (int retryCount = 1; retryCount <= MAX_STARTUP_RETRIES; retryCount++) {
            log.info("第 {}/{} 次尝试连接AI服务...", retryCount, MAX_STARTUP_RETRIES);

            if (performFullHealthCheck()) {
                return true;
            }

            if (retryCount < MAX_STARTUP_RETRIES) {
                log.warn("AI服务暂不可用，{}秒后重试...", STARTUP_RETRY_INTERVAL_MS / 1000);
                try {
                    Thread.sleep(STARTUP_RETRY_INTERVAL_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("健康检查被中断");
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * 定时健康检查（每60秒执行一次）
     */
    @Scheduled(fixedRate = 60000, initialDelay = 120000)
    public void scheduledHealthCheck() {
        boolean wasAvailable = aiServiceAvailable.get();
        boolean isAvailable = performQuickHealthCheck();

        if (isAvailable) {
            if (!wasAvailable) {
                log.info("AI服务已恢复连接");
            }
            aiServiceAvailable.set(true);
            consecutiveFailures.set(0);
        } else {
            int failures = consecutiveFailures.incrementAndGet();
            aiServiceAvailable.set(false);

            if (failures >= FAILURE_ALERT_THRESHOLD) {
                log.error("AI服务连续{}次健康检查失败，请检查服务状态！地址: {}",
                        failures, aiServiceProperties.getBaseUrl());
            } else {
                log.warn("AI服务健康检查失败，连续失败次数: {}", failures);
            }
        }
    }

    /**
     * 执行完整的健康检查（检查所有端点）
     *
     * @return 是否全部通过
     */
    private boolean performFullHealthCheck() {
        String baseUrl = aiServiceProperties.getBaseUrl();

        // 检查根路径
        HealthCheckResult rootResult = checkEndpoint(baseUrl + "/", "根路径探活");
        if (!rootResult.isSuccess()) {
            log.warn("根路径探活失败: {}", rootResult.getMessage());
        } else {
            log.info("✓ 根路径探活成功: {}", rootResult.getMessage());
        }

        // 检查 /health 端点
        HealthCheckResult healthResult = checkEndpoint(baseUrl + "/health", "通用健康检查");
        if (!healthResult.isSuccess()) {
            log.warn("/health 端点检查失败: {}", healthResult.getMessage());
        } else {
            log.info("✓ /health 端点检查成功: {}", healthResult.getMessage());
        }

        // 检查 /api/v1/health 端点
        HealthCheckResult v1HealthResult = checkEndpoint(baseUrl + "/api/v1/health", "V1健康检查");
        if (!v1HealthResult.isSuccess()) {
            log.warn("/api/v1/health 端点检查失败: {}", v1HealthResult.getMessage());
        } else {
            log.info("✓ /api/v1/health 端点检查成功: {}", v1HealthResult.getMessage());
        }

        // 至少有一个端点成功即认为服务可用
        return rootResult.isSuccess() || healthResult.isSuccess() || v1HealthResult.isSuccess();
    }

    /**
     * 执行快速健康检查（只检查主要端点）
     *
     * @return 是否通过
     */
    private boolean performQuickHealthCheck() {
        String baseUrl = aiServiceProperties.getBaseUrl();

        // 优先检查 /api/v1/health
        HealthCheckResult result = checkEndpoint(baseUrl + "/api/v1/health", "V1健康检查");
        if (result.isSuccess()) {
            return true;
        }

        // 备选检查 /health
        result = checkEndpoint(baseUrl + "/health", "通用健康检查");
        return result.isSuccess();
    }

    /**
     * 检查单个端点
     *
     * @param url         端点URL
     * @param description 端点描述
     * @return 检查结果
     */
    private HealthCheckResult checkEndpoint(String url, String description) {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = healthCheckClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String body = response.body().string();
                String statusInfo = parseHealthResponse(body);
                return new HealthCheckResult(true,
                        String.format("[%s] HTTP %d - %s", description, response.code(), statusInfo));
            } else {
                return new HealthCheckResult(false,
                        String.format("[%s] HTTP %d", description, response.code()));
            }
        } catch (Exception e) {
            return new HealthCheckResult(false, String.format("[%s] %s", description, e.getMessage()));
        }
    }

    /**
     * 解析健康检查响应
     *
     * @param body 响应体
     * @return 状态信息
     */
    private String parseHealthResponse(String body) {
        try {
            JsonNode root = objectMapper.readTree(body);

            StringBuilder info = new StringBuilder();

            // 尝试获取状态
            if (root.has(FIELD_STATUS)) {
                info.append("status=").append(root.get(FIELD_STATUS).asText());
            }

            // 尝试获取服务名称
            if (root.has(FIELD_SERVICE)) {
                if (!info.isEmpty()) {
                    info.append(", ");
                }
                info.append("service=").append(root.get(FIELD_SERVICE).asText());
            }

            // 尝试获取版本
            if (root.has(FIELD_VERSION)) {
                if (!info.isEmpty()) {
                    info.append(", ");
                }
                info.append("version=").append(root.get(FIELD_VERSION).asText());
            }

            // 尝试获取消息
            if (root.has(FIELD_MESSAGE)) {
                if (!info.isEmpty()) {
                    info.append(", ");
                }
                info.append("message=").append(root.get(FIELD_MESSAGE).asText());
            }

            return !info.isEmpty() ? info.toString() : body;
        } catch (Exception e) {
            return body.length() > 100 ? body.substring(0, 100) + "..." : body;
        }
    }

    /**
     * 获取AI服务是否可用
     * <p>
     * 此方法提供给其他组件查询AI服务状态
     * </p>
     *
     * @return 是否可用
     */
    @SuppressWarnings("unused")
    public boolean isAiServiceAvailable() {
        return aiServiceAvailable.get();
    }

    /**
     * 获取连续失败次数
     * <p>
     * 此方法提供给监控组件获取失败统计
     * </p>
     *
     * @return 连续失败次数
     */
    @SuppressWarnings("unused")
    public int getConsecutiveFailures() {
        return consecutiveFailures.get();
    }

    /**
     * 手动触发健康检查
     * <p>
     * 此方法提供给管理接口手动触发检查
     * </p>
     *
     * @return 检查结果
     */
    @SuppressWarnings("unused")
    public boolean triggerHealthCheck() {
        log.info("手动触发AI服务健康检查");
        boolean result = performFullHealthCheck();
        aiServiceAvailable.set(result);
        if (result) {
            consecutiveFailures.set(0);
        } else {
            consecutiveFailures.incrementAndGet();
        }
        return result;
    }

    /**
     * 健康检查结果
     */
    @lombok.Getter
    private static class HealthCheckResult {
        private final boolean success;
        private final String message;

        public HealthCheckResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
}

