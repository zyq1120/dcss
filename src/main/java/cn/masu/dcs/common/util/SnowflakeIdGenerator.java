package cn.masu.dcs.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 雪花算法ID生成器
 * <p>
 * Twitter的Snowflake算法实现，生成64位的唯一ID
 * ID结构：1位符号位 + 41位时间戳 + 10位机器ID + 12位序列号
 * </p>
 * <p>
 * 特点：
 * 1. 趋势递增：时间戳在高位，ID整体递增
 * 2. 不重复：同一毫秒内通过序列号区分
 * 3. 高性能：本地生成，无需网络通信
 * 4. 有序性：按时间有序，便于索引
 * </p>
 *
 * @author zyq
 * @since 2025-12-06
 */
@Slf4j
@Component
public class SnowflakeIdGenerator {

    /**
     * 起始时间戳 (2020-01-01 00:00:00)
     */
    private static final long TWEPOCH = 1577836800000L;

    /**
     * 机器ID所占位数
     */
    private static final long WORKER_ID_BITS = 5L;

    /**
     * 数据中心ID所占位数
     */
    private static final long DATACENTER_ID_BITS = 5L;

    /**
     * 序列号所占位数
     */
    private static final long SEQUENCE_BITS = 12L;

    /**
     * 支持的最大机器ID (0-31)
     */
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);

    /**
     * 支持的最大数据中心ID (0-31)
     */
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);

    /**
     * 机器ID左移位数
     */
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;

    /**
     * 数据中心ID左移位数
     */
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

    /**
     * 时间戳左移位数
     */
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;

    /**
     * 序列号掩码 (0-4095)
     */
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    /**
     * 工作机器ID (0-31)
     */
    private final long workerId;

    /**
     * 数据中心ID (0-31)
     */
    private final long datacenterId;

    /**
     * 毫秒内序列号 (0-4095)
     */
    private long sequence = 0L;

    /**
     * 上次生成ID的时间戳
     */
    private long lastTimestamp = -1L;

    /**
     * 默认构造函数
     * <p>
     * workerId和datacenterId都设为0
     * </p>
     */
    public SnowflakeIdGenerator() {
        this(0L, 0L);
    }

    /**
     * 构造函数
     *
     * @param workerId     工作机器ID (0-31)
     * @param datacenterId 数据中心ID (0-31)
     * @throws IllegalArgumentException 当workerId或datacenterId超出范围时
     */
    public SnowflakeIdGenerator(long workerId, long datacenterId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException(
                    String.format("Worker ID不能大于%d或小于0", MAX_WORKER_ID));
        }
        if (datacenterId > MAX_DATACENTER_ID || datacenterId < 0) {
            throw new IllegalArgumentException(
                    String.format("Datacenter ID不能大于%d或小于0", MAX_DATACENTER_ID));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;

        log.info("SnowflakeIdGenerator初始化: workerId={}, datacenterId={}", workerId, datacenterId);
    }

    /**
     * 获得下一个ID (线程安全)
     * <p>
     * 使用synchronized确保线程安全
     * </p>
     *
     * @return 唯一ID
     * @throws RuntimeException 当时钟回退时抛出异常
     */
    public synchronized long nextId() {
        long timestamp = getCurrentTimestamp();

        // 时钟回退检测
        if (timestamp < lastTimestamp) {
            long offset = lastTimestamp - timestamp;
            log.error("时钟回退检测: 回退了{}毫秒", offset);
            throw new RuntimeException(
                    String.format("时钟回退。拒绝生成ID，回退时间: %d毫秒", offset));
        }

        // 同一毫秒内，序列号递增
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;

            // 毫秒内序列号溢出（超过4095）
            if (sequence == 0) {
                // 阻塞到下一毫秒，获得新的时间戳
                timestamp = waitNextMillis(lastTimestamp);
                log.debug("序列号溢出，等待下一毫秒: timestamp={}", timestamp);
            }
        } else {
            // 时间戳改变，序列号重置为0
            sequence = 0L;
        }

        // 更新上次生成ID的时间戳
        lastTimestamp = timestamp;

        // 组装64位ID
        // 1位符号位(0) + 41位时间戳 + 5位数据中心ID + 5位机器ID + 12位序列号
        return ((timestamp - TWEPOCH) << TIMESTAMP_LEFT_SHIFT)
                | (datacenterId << DATACENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    /**
     * 阻塞到下一毫秒，直到获得新的时间戳
     *
     * @param lastTimestamp 上次生成ID的时间戳
     * @return 当前时间戳
     */
    private long waitNextMillis(long lastTimestamp) {
        long timestamp = getCurrentTimestamp();
        while (timestamp <= lastTimestamp) {
            timestamp = getCurrentTimestamp();
        }
        return timestamp;
    }

    /**
     * 获取当前时间戳（毫秒）
     *
     * @return 当前时间戳
     */
    private long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

    /**
     * 获取工作机器ID
     *
     * @return 工作机器ID
     */
    public long getWorkerId() {
        return workerId;
    }

    /**
     * 获取数据中心ID
     *
     * @return 数据中心ID
     */
    public long getDatacenterId() {
        return datacenterId;
    }

    /**
     * 解析ID获取时间戳
     * <p>
     * 从生成的ID中提取时间戳部分
     * </p>
     *
     * @param id 雪花ID
     * @return 时间戳（毫秒）
     */
    public long getTimestampFromId(long id) {
        return (id >> TIMESTAMP_LEFT_SHIFT) + TWEPOCH;
    }

    /**
     * 解析ID获取工作机器ID
     *
     * @param id 雪花ID
     * @return 工作机器ID
     */
    public long getWorkerIdFromId(long id) {
        return (id >> WORKER_ID_SHIFT) & MAX_WORKER_ID;
    }

    /**
     * 解析ID获取数据中心ID
     *
     * @param id 雪花ID
     * @return 数据中心ID
     */
    public long getDatacenterIdFromId(long id) {
        return (id >> DATACENTER_ID_SHIFT) & MAX_DATACENTER_ID;
    }

    /**
     * 解析ID获取序列号
     *
     * @param id 雪花ID
     * @return 序列号
     */
    public long getSequenceFromId(long id) {
        return id & SEQUENCE_MASK;
    }
}

