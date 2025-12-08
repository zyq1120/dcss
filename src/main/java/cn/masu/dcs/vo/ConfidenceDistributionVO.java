package cn.masu.dcs.vo;

import lombok.Data;
import java.util.List;

/**
 * 置信度分布VO
 *
 * @author zyq
 * @since 2025-12-07
 */
@Data
public class ConfidenceDistributionVO {

    /**
     * 置信度区间
     */
    private List<String> ranges;

    /**
     * 数量
     */
    private List<Long> counts;

    /**
     * 百分比
     */
    private List<Double> percentages;
}

