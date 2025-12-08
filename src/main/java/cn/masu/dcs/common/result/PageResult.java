package cn.masu.dcs.common.result;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果封装
 * <p>
 * 用于封装分页查询的结果数据
 * </p>
 * <p>
 * 包含字段：
 * - total: 总记录数
 * - current: 当前页码
 * - size: 每页大小
 * - records: 当前页数据列表
 * </p>
 *
 * @author zyq
 * @since 2025-12-06
 */
@Data
public class PageResult<T> implements Serializable {

    @java.io.Serial
    private static final long serialVersionUID = 1L;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Long current;

    /**
     * 每页大小
     */
    private Long size;

    /**
     * 当前页数据列表
     */
    private List<T> records;

    /**
     * 静态工厂方法：创建分页结果
     *
     * @param total   总记录数
     * @param current 当前页码
     * @param size    每页大小
     * @param records 当前页数据列表
     * @param <T>     数据类型
     * @return 分页结果对象
     */
    public static <T> PageResult<T> of(Long total, Long current, Long size, List<T> records) {
        PageResult<T> result = new PageResult<>();
        result.setTotal(total);
        result.setCurrent(current);
        result.setSize(size);
        result.setRecords(records);
        return result;
    }

    /**
     * 从MyBatis-Plus的Page对象转换
     *
     * @param page MyBatis-Plus的Page对象
     * @param <T>  数据类型
     * @return 分页结果对象
     */
    public static <T> PageResult<T> of(com.baomidou.mybatisplus.extension.plugins.pagination.Page<T> page) {
        PageResult<T> result = new PageResult<>();
        result.setTotal(page.getTotal());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        result.setRecords(page.getRecords());
        return result;
    }

    /**
     * 计算总页数
     *
     * @return 总页数
     */
    public Long getPages() {
        if (size == null || size == 0) {
            return 0L;
        }
        long pages = total / size;
        if (total % size != 0) {
            pages++;
        }
        return pages;
    }

    /**
     * 是否有上一页
     *
     * @return true-有上一页，false-无上一页
     */
    public boolean hasPrevious() {
        return current != null && current > 1;
    }

    /**
     * 是否有下一页
     *
     * @return true-有下一页，false-无下一页
     */
    public boolean hasNext() {
        return current != null && current < getPages();
    }
}

