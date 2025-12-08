package cn.masu.dcs.vo;

import lombok.Data;
import java.util.List;

/**
 * 文件类型分布VO
 *
 * @author zyq
 * @since 2025-12-07
 */
@Data
public class FileTypeDistributionVO {

    /**
     * 文件类型列表
     */
    private List<String> types;

    /**
     * 数量列表
     */
    private List<Long> counts;

    /**
     * 百分比列表
     */
    private List<Double> percentages;
}

