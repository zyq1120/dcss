package cn.masu.dcs.vo;

import lombok.Data;
import java.util.List;

/**
 * 处理状态分布VO
 *
 * @author zyq
 * @since 2025-12-07
 */
@Data
public class StatusDistributionVO {

    /**
     * 状态名称列表
     */
    private List<String> statusNames;

    /**
     * 数量列表
     */
    private List<Long> counts;

    /**
     * 百分比列表
     */
    private List<Double> percentages;
}

