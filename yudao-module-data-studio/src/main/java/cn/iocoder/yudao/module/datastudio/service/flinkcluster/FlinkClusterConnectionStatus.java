package cn.iocoder.yudao.module.datastudio.service.flinkcluster;

import lombok.Data;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

/**
 * Flink集群连接状态实体
 */
@Data
public class FlinkClusterConnectionStatus {

    /**
     * 集群ID
     */
    private Long clusterId;

    /**
     * 集群名称
     */
    private String clusterName;

    /**
     * 是否已连接
     */
    private volatile boolean connected = false;

    /**
     * 最后心跳时间戳
     */
    private volatile long lastHeartbeatTime = 0;

    /**
     * 连接超时时间(毫秒)
     */
    private int connectTimeout;

    /**
     * 心跳间隔(秒)
     */
    private int heartbeatInterval;

    /**
     * 心跳调度器
     */
    private ScheduledExecutorService heartbeatScheduler;

    /**
     * 心跳任务
     */
    private ScheduledFuture<?> heartbeatTask;

    /**
     * 集群类型：remote或yarn
     */
    private String clusterType;
}
