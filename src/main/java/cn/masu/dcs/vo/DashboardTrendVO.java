package cn.masu.dcs.vo;

import lombok.Data;
import java.util.List;

/**
 * 趋势数据VO
 *
 * @author zyq
 * @since 2025-12-07
 */
@Data
public class DashboardTrendVO {

    /**
     * 日期列表
     */
    private List<String> dates;

    /**
     * 文件数量趋势
     */
    private List<Long> fileCount;

    /**
     * 平均置信度趋势
     */
    private List<Double> avgConfidence;

    /**
     * 成功数趋势
     */
    private List<Long> successCount;

    /**
     * 失败数趋势
     */
    private List<Long> failCount;

    /**
     * 待审核数趋势
     */
    private List<Long> reviewCount;
}

