package cn.iocoder.yudao.module.flink.common.constants;

/**
 * Flink常量类
 *
 * @author yzh
 * @since 2025-11-27
 */
public class FlinkConstants {

    /**
     * Flink配置默认并行度
     */
    public static final int DEFAULT_PARALLELISM = 1;

    /**
     * Flink默认端口
     */
    public static final int DEFAULT_PORT = 8081;

    /**
     * Flink历史服务器端口
     */
    public static final int DEFAULT_HISTORY_PORT = 8082;

    /**
     * 作业提交超时时间（毫秒）
     */
    public static final long SUBMIT_TIMEOUT_MS = 60000L;

    /**
     * 作业状态检查间隔（毫秒）
     */
    public static final long STATUS_CHECK_INTERVAL_MS = 5000L;

}
