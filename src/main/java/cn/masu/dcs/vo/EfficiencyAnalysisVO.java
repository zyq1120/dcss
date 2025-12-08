package cn.masu.dcs.vo;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 效率分析VO
 *
 * @author zyq
 * @since 2025-12-07
 */
@Data
public class EfficiencyAnalysisVO {

    /**
     * 处理速度分析
     */
    private ProcessingSpeed processingSpeed;

    /**
     * 质量分析
     */
    private QualityAnalysis qualityAnalysis;

    /**
     * 人工干预分析
     */
    private HumanInterventionAnalysis humanInterventionAnalysis;

    /**
     * 处理速度分析
     */
    @Data
    public static class ProcessingSpeed {
        /**
         * 平均处理时间（秒）
         */
        private Double avgProcessTime;

        /**
         * 最快处理时间（秒）
         */
        private Double minProcessTime;

        /**
         * 最慢处理时间（秒）
         */
        private Double maxProcessTime;

        /**
         * 每小时处理量
         */
        private Double filesPerHour;
    }

    /**
     * 质量分析
     */
    @Data
    public static class QualityAnalysis {
        /**
         * 高置信度比例（>0.9）
         */
        private Double highConfidenceRate;

        /**
         * 中置信度比例（0.7-0.9）
         */
        private Double mediumConfidenceRate;

        /**
         * 低置信度比例（<0.7）
         */
        private Double lowConfidenceRate;

        /**
         * 平均置信度
         */
        private Double avgConfidence;
    }

    /**
     * 人工干预分析
     */
    @Data
    public static class HumanInterventionAnalysis {
        /**
         * 需要人工审核的比例
         */
        private Double reviewRate;

        /**
         * 平均审核时间（分钟）
         */
        private Double avgReviewTime;

        /**
         * 字段修改率
         */
        private Double fieldModificationRate;

        /**
         * 最常修改的字段
         */
        private List<Map<String, Object>> topModifiedFields;
    }
}

