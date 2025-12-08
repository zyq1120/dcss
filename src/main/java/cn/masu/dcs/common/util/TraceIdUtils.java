package cn.masu.dcs.common.util;

import org.slf4j.MDC;

import java.util.UUID;

/**
 * 链路追踪ID工具类
 * <p>
 * 用于在日志中记录请求的唯一标识，便于分布式系统的日志追踪
 * 使用SLF4J的MDC（Mapped Diagnostic Context）实现
 * </p>
 * <p>
 * 使用场景：
 * 1. 在Filter/Interceptor中为每个请求生成traceId
 * 2. 在日志配置中输出traceId
 * 3. 在微服务调用时传递traceId
 * </p>
 *
 * @author zyq
 * @since 2025-12-06
 */
public class TraceIdUtils {

    /**
     * MDC中存储traceId的key
     */
    private static final String TRACE_ID = "traceId";

    /**
     * 私有构造函数，防止实例化
     */
    private TraceIdUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 生成追踪ID
     * <p>
     * 使用UUID生成32位唯一ID（去除横线）
     * </p>
     *
     * @return 追踪ID字符串
     */
    public static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 设置追踪ID到MDC
     * <p>
     * 将traceId存储到当前线程的MDC中，后续的日志都会包含此traceId
     * </p>
     *
     * @param traceId 追踪ID
     */
    public static void setTraceId(String traceId) {
        if (traceId != null && !traceId.isEmpty()) {
            MDC.put(TRACE_ID, traceId);
        }
    }

    /**
     * 获取当前的追踪ID
     * <p>
     * 如果当前线程的MDC中不存在traceId，则自动生成一个新的
     * </p>
     *
     * @return 追踪ID字符串
     */
    public static String getTraceId() {
        String traceId = MDC.get(TRACE_ID);
        if (traceId == null) {
            traceId = generateTraceId();
            setTraceId(traceId);
        }
        return traceId;
    }

    /**
     * 清除当前线程的追踪ID
     * <p>
     * 在请求处理完成后调用，避免内存泄漏
     * 注意：使用线程池时必须调用此方法清理MDC
     * </p>
     */
    public static void clearTraceId() {
        MDC.remove(TRACE_ID);
    }

    /**
     * 清除MDC中的所有内容
     * <p>
     * 彻底清理当前线程的MDC，防止线程池复用导致的数据污染
     * </p>
     */
    public static void clear() {
        MDC.clear();
    }
}

