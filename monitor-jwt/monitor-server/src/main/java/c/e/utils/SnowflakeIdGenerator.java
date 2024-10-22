package c.e.utils;

import org.springframework.stereotype.Component;

/**
 * 雪花算法ID生成器
 */
@Component
public class SnowflakeIdGenerator {

    //自定义的起始时间戳
    private static final long START_TIMESTAMP = 1691087910202L;

    //数据中心id的位数
    private static final long DATA_CENTER_ID_BITS = 5L;

    //工作节点id的位数
    private static final long WORKER_ID_BITS = 5L;

    //序列号位数
    private static final long SEQUENCE_BITS =12L;

    //计算数据中心最大值哦          ~(1L << n)的意思是将负数左移n位，产生一个全1的位掩码
    private static final long MAX_DATA_CENTER_ID = ~(-1L << DATA_CENTER_ID_BITS);

    //计算工作节点id最大值
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    //计算序列号的最大值
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

    //使用时间戳、数据中心id。工作节点ID和序列号组合生成唯一ID。ID的各个部分通过位移操作拼接
    //这些常量用于在生成ID时对不同部分进行位移，以确保每个部分在最终ID中占据不同的位数
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long DATA_CENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATA_CENTER_ID_BITS;

    private final long dataCenterId;
    private final long workerId;
    private long lastTimestamp = -1L;
    private long sequence = 0L;

    //提供默认构造函数和带参数的构造函数，确保数据中心ID和工作节点ID在合法范围内。
    public SnowflakeIdGenerator(){
        this(1, 1);
    }

    private SnowflakeIdGenerator(long dataCenterId, long workerId) {
        //检查传入的参数是否合法
        if (dataCenterId > MAX_DATA_CENTER_ID || dataCenterId < 0) {
            throw new IllegalArgumentException("Data center ID can't be greater than " + MAX_DATA_CENTER_ID + " or less than 0");
        }
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException("Worker ID can't be greater than " + MAX_WORKER_ID + " or less than 0");
        }
        this.dataCenterId = dataCenterId;
        this.workerId = workerId;
    }

    /**
     * 生成一个新的雪花算法ID加锁
     * @return 雪花ID
     */
    public synchronized long nextId() {
        //生成新ID时，首先获取当前时间戳，确保其不小于上次生成ID的时间戳
        long timestamp = getCurrentTimestamp();
        //如果当前时间戳小鱼上次生成ID的时间戳，抛出异常以防止生成重复ID
        if (timestamp < lastTimestamp) {
            throw new IllegalStateException("Clock moved backwards. Refusing to generate ID.");
        }
        //如果时间戳相同，增加序列号，如果序列号达到最大值，则调用getNextTimestamp等待下一个时间戳
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                timestamp = getNextTimestamp(lastTimestamp);
            }
        } else {
            //如果当前时间戳大于上次时间戳，重置序列号为0
            sequence = 0L;
        }
        lastTimestamp = timestamp;
        //组合返回生成的ID    通过位运算将时间戳、数据中心ID、工作节点ID和序列号拼接成最终的ID。
        return ((timestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT) |
                (dataCenterId << DATA_CENTER_ID_SHIFT) |
                (workerId << WORKER_ID_SHIFT) |
                sequence;
    }

    //获取当前的毫秒时间戳
    private long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

    //确保生成的时间戳在上次时间戳之后                  如果系统时钟回拨，循环等待直到获取到一个新的时间戳，确保ID的一致性。
    private long getNextTimestamp(long lastTimestamp) {
        long timestamp = getCurrentTimestamp();
        while (timestamp <= lastTimestamp) {
            timestamp = getCurrentTimestamp();
        }
        return timestamp;
    }

}
