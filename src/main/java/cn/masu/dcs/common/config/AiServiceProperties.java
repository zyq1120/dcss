package cn.masu.dcs.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author zyq
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai.service")
public class AiServiceProperties {
    private String baseUrl;
    private boolean enableLlmFallback = true;
    private double dailyBudget = 20.0;
    private double ocrConfidenceThreshold = 0.7;
    private double nlpConfidenceThreshold = 0.75;
    private String llmModel;
    private String llmProvider;
}

