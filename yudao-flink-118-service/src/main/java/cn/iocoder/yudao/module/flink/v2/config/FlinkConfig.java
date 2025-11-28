package cn.iocoder.yudao.module.flink.v2.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * Flink V2配置类
 *
 * @author yzh
 * @since 2025-11-27
 */
@Data
@Component
@ConfigurationProperties(prefix = "flink.v2")
@RefreshScope
public class FlinkConfig {

    /**
     * Flink集群地址
     */
    private String clusterAddress;

    /**
     * Flink历史服务器地址
     */
    private String historyAddress;

    /**
     * 默认并行度
     */
    private Integer parallelism = 1;

    /**
     * 作业提交超时时间（毫秒）
     */
    private Long submitTimeout = 60000L;

}
