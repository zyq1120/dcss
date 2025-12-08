package cn.masu.dcs.vo;

import lombok.Data;

/**
 * 驾驶舱概览VO
 *
 * @author zyq
 * @since 2025-12-07
 */
@Data
public class DashboardOverviewVO {

    /**
     * 文件统计
     */
    private FileStats fileStats;

    /**
     * 任务统计
     */
    private TaskStats taskStats;

    /**
     * 效率统计
     */
    private EfficiencyStats efficiencyStats;

    /**
     * 文件统计
     */
    @Data
    public static class FileStats {
        /**
         * 文件总数
         */
        private Long total;

        /**
         * 已处理数
         */
        private Long processed;

        /**
         * 待处理数
         */
        private Long pending;

        /**
         * 待人工审核数
         */
        private Long needReview;

        /**
         * 已归档数
         */
        private Long archived;

        /**
         * 失败数
         */
        private Long failed;
    }

    /**
     * 任务统计
     */
    @Data
    public static class TaskStats {
        /**
         * 任务总数
         */
        private Long total;

        /**
         * 成功数
         */
        private Long success;

        /**
         * 失败数
         */
        private Long failed;

        /**
         * 平均置信度
         */
        private Double avgConfidence;

        /**
         * 今日新增
         */
        private Long todayCount;

        /**
         * 本周新增
         */
        private Long weekCount;
    }

    /**
     * 效率统计
     */
    @Data
    public static class EfficiencyStats {
        /**
         * AI平均处理时间（秒）
         */
        private Double avgAiTime;

        /**
         * 人工平均校对时间（秒）
         */
        private Double avgHumanTime;

        /**
         * 直通率（无需人工审核的比例）
         */
        private Double directPassRate;

        /**
         * 平均重试次数
         */
        private Double avgRetryCount;
    }
}

