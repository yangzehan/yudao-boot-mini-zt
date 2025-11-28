package cn.iocoder.yudao.module.flink.common.enums;

/**
 * Flink作业状态枚举
 *
 * @author yzh
 * @since 2025-11-27
 */
public enum FlinkJobStatusEnum {

    /**
     * 初始化
     */
    INITIALIZING("INITIALIZING", "初始化"),

    /**
     * 运行中
     */
    RUNNING("RUNNING", "运行中"),

    /**
     * 已完成
     */
    FINISHED("FINISHED", "已完成"),

    /**
     * 已取消
     */
    CANCELED("CANCELED", "已取消"),

    /**
     * 已暂停
     */
    SUSPENDED("SUSPENDED", "已暂停"),

    /**
     * 重启中
     */
    RESTARTING("RESTARTING", "重启中"),

    /**
     * 失败
     */
    FAILED("FAILED", "失败"),

    /**
     * 未知
     */
    UNKNOWN("UNKNOWN", "未知");

    private final String code;
    private final String desc;

    FlinkJobStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
