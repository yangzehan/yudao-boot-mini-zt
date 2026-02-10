package cn.iocoder.yudao.module.flink.monitor.alert.model;

/**
 * 告警级别枚举
 *
 * @author yzh
 */
public enum AlertLevel {

    /**
     * 信息
     */
    INFO("信息", 1),

    /**
     * 警告
     */
    WARNING("警告", 2),

    /**
     * 严重
     */
    CRITICAL("严重", 3);

    private final String description;
    private final int priority;

    AlertLevel(String description, int priority) {
        this.description = description;
        this.priority = priority;
    }

    public String getDescription() {
        return description;
    }

    public int getPriority() {
        return priority;
    }

    /**
     * 是否高于指定级别
     */
    public boolean isHigherThan(AlertLevel other) {
        return this.priority > other.priority;
    }
}
