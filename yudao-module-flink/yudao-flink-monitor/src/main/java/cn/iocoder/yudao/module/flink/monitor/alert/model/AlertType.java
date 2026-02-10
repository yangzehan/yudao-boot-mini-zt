package cn.iocoder.yudao.module.flink.monitor.alert.model;

/**
 * 告警类型枚举
 *
 * @author yzh
 */
public enum AlertType {

    /**
     * 作业失败
     */
    JOB_FAILED("作业失败", "JOB_FAILED"),

    /**
     * 作业取消
     */
    JOB_CANCELLED("作业取消", "JOB_CANCELLED"),

    /**
     * 作业重启
     */
    JOB_RESTART("作业重启", "JOB_RESTART"),

    /**
     * 检查点失败
     */
    CHECKPOINT_FAILED("检查点失败", "CHECKPOINT_FAILED"),

    /**
     * 背压告警
     */
    BACKPRESSURE("背压告警", "BACKPRESSURE"),

    /**
     * 作业延迟
     */
    JOB_SLOW("作业延迟", "JOB_SLOW"),

    /**
     * 未知异常
     */
    UNKNOWN("未知异常", "UNKNOWN");

    private final String description;
    private final String code;

    AlertType(String description, String code) {
        this.description = description;
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public String getCode() {
        return code;
    }
}
