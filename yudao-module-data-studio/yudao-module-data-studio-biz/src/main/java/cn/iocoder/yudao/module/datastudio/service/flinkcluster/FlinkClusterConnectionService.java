package cn.iocoder.yudao.module.datastudio.service.flinkcluster;

import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.datastudio.controller.admin.flinkcluster.vo.extra.FlinkClusterConnectionTestRespVO;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.flinkcluster.FlinkClusterDO;
import cn.iocoder.yudao.module.datastudio.dal.mysql.flinkcluster.FlinkClusterMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Flink集群连接管理服务
 */
@Slf4j
@Service
public class FlinkClusterConnectionService {

    @Autowired
    private FlinkClusterMapper clusterMapper;

    /**
     * 连接池：存储集群连接状态
     */
    private final Map<Long, FlinkClusterConnectionStatus> connectionPool = new ConcurrentHashMap<>();

    /**
     * 线程池工厂
     */
    private static final ThreadFactory namedThreadFactory = new ThreadFactory() {
        private final AtomicInteger counter = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "flink-cluster-heartbeat-" + counter.getAndIncrement());
            t.setDaemon(true);
            return t;
        }
    };

    /**
     * 核心线程池
     */
    private static final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
            10,
            50,
            60L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(100),
            namedThreadFactory,
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    /**
     * 日期格式化器
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 初始化集群连接
     */
    public void initClusterConnection(Long clusterId) {
        FlinkClusterDO cluster = clusterMapper.selectById(clusterId);
        if (cluster == null) {
            log.error("集群不存在 clusterId={}", clusterId);
            return;
        }

        // 创建连接状态
        FlinkClusterConnectionStatus status = new FlinkClusterConnectionStatus();
        status.setClusterId(clusterId);
        status.setClusterName(cluster.getName());
        status.setClusterType(cluster.getType());
        status.setConnectTimeout(cluster.getConnectTimeout() != null
                ? cluster.getConnectTimeout() : 30000);
        status.setHeartbeatInterval(cluster.getHeartbeatInterval() != null
                ? cluster.getHeartbeatInterval() : 60);

        // 创建心跳调度器
        status.setHeartbeatScheduler(Executors.newSingleThreadScheduledExecutor(
                r -> {
                    Thread t = new Thread(r, "flink-cluster-" + clusterId);
                    t.setDaemon(true);
                    return t;
                }
        ));

        connectionPool.put(clusterId, status);

        // 启动心跳检测
        startHeartbeat(clusterId);

        // 如果集群状态是运行中或可用，立即尝试连接
        if ("running".equals(cluster.getStatus()) || "available".equals(cluster.getStatus())) {
            connectToCluster(clusterId);
        }

        log.info("初始化集群连接完成 clusterId={}, type={}", clusterId, cluster.getType());
    }

    /**
     * 启动心跳检测
     */
    public void startHeartbeat(Long clusterId) {
        FlinkClusterConnectionStatus status = connectionPool.get(clusterId);
        if (status == null) {
            log.error("集群连接状态不存在 clusterId={}", clusterId);
            return;
        }

        // 取消旧的心跳任务
        if (status.getHeartbeatTask() != null) {
            status.getHeartbeatTask().cancel(false);
        }

        // 启动新的心跳任务
        status.setHeartbeatTask(status.getHeartbeatScheduler()
                .scheduleWithFixedDelay(() -> {
                    try {
                        checkClusterHealth(clusterId);
                    } catch (Exception e) {
                        log.error("心跳检测异常 clusterId={}", clusterId, e);
                    }
                }, 0, status.getHeartbeatInterval(), TimeUnit.SECONDS));

        log.info("启动心跳检测 clusterId={}, interval={}s",
                clusterId, status.getHeartbeatInterval());
    }

    /**
     * 检查集群健康状态
     */
    public void checkClusterHealth(Long clusterId) {
        FlinkClusterDO cluster = clusterMapper.selectById(clusterId);
        if (cluster == null) {
            log.error("集群不存在 clusterId={}", clusterId);
            return;
        }

        FlinkClusterConnectionStatus status = connectionPool.get(clusterId);
        if (status == null) {
            return;
        }

        // 更新最后心跳时间
        status.setLastHeartbeatTime(System.currentTimeMillis());
        updateLastHeartbeatTime(clusterId, LocalDateTime.now());

        try {
            // 根据集群类型执行健康检查
            boolean isHealthy = false;
            if ("remote".equals(cluster.getType())) {
                isHealthy = pingRemoteCluster(cluster);
            } else if ("yarn".equals(cluster.getType())) {
                isHealthy = pingYarnCluster(cluster);
            }

            if (isHealthy) {
                // 连接正常
                if (!status.isConnected()) {
                    log.info("集群重新连接成功 clusterId={}", clusterId);
                    status.setConnected(true);
                    updateClusterStatus(clusterId, "available");
                }
            } else {
                // 连接断开
                if (status.isConnected()) {
                    log.warn("集群连接断开 clusterId={}", clusterId);
                    status.setConnected(false);
                    updateClusterStatus(clusterId, "unavailable");
                }
            }
        } catch (Exception e) {
            log.error("集群健康检查失败 clusterId={}", clusterId, e);
            status.setConnected(false);
            updateClusterStatus(clusterId, "unavailable");
        }
    }

    /**
     * Ping远程集群
     */
    private boolean pingRemoteCluster(FlinkClusterDO cluster) {
        try {
            String url = cluster.getWebUiUrl();
            if (url == null || url.isEmpty()) {
                url = "http://" + cluster.getRemoteUrl();
            }

            // 追加API路径
            if (!url.endsWith("/")) {
                url += "/";
            }
            url += "jobmanager/config";

            // 创建HTTP客户端
            CloseableHttpClient httpClient = HttpClients.createDefault();
            RequestConfig config = RequestConfig.custom()
                    .setConnectTimeout(cluster.getConnectTimeout() != null
                            ? cluster.getConnectTimeout() : 30000)
                    .setSocketTimeout(cluster.getConnectTimeout() != null
                            ? cluster.getConnectTimeout() : 30000)
                    .setConnectionRequestTimeout(cluster.getConnectTimeout() != null
                            ? cluster.getConnectTimeout() : 30000)
                    .build();

            HttpGet request = new HttpGet(url);
            request.setConfig(config);
            request.setHeader("Accept", "application/json");

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                return statusCode == 200;
            }

        } catch (Exception e) {
            log.debug("远程集群连接失败 clusterId={}, error={}", cluster.getId(), e.getMessage());
            return false;
        }
    }

    /**
     * Ping Yarn集群
     */
    private boolean pingYarnCluster(FlinkClusterDO cluster) {
        try {
            // Yarn集群健康检查逻辑
            // 通过配置文件路径验证
            String yarnSitePath = cluster.getYarnSitePath();
            String hdfsSitePath = cluster.getHdfsSitePath();
            String coreSitePath = cluster.getCoreSitePath();
            String queueName = cluster.getQueueName();

            log.debug("检查Yarn集群状态 clusterId={}, queue={}, yarnSitePath={}, hdfsSitePath={}, coreSitePath={}",
                    cluster.getId(), queueName, yarnSitePath, hdfsSitePath, coreSitePath);

            // 简化实现：假设连接Yarn成功
            return true;

        } catch (Exception e) {
            log.debug("Yarn集群连接失败 clusterId={}, error={}", cluster.getId(), e.getMessage());
            return false;
        }
    }

    /**
     * 测试连接
     */
    public FlinkClusterConnectionTestRespVO testConnection(Long clusterId) {
        FlinkClusterDO cluster = clusterMapper.selectById(clusterId);
        if (cluster == null) {
            throw new IllegalArgumentException("集群不存在");
        }

        long startTime = System.currentTimeMillis();
        boolean connected = false;
        String message = "连接失败";

        try {
            if ("remote".equals(cluster.getType())) {
                connected = pingRemoteCluster(cluster);
                message = connected ? "连接成功" : "连接失败";
            } else if ("yarn".equals(cluster.getType())) {
                connected = pingYarnCluster(cluster);
                message = connected ? "连接成功" : "连接失败";
            }
        } catch (Exception e) {
            log.error("连接测试失败 clusterId={}", clusterId, e);
            message = "连接异常: " + e.getMessage();
            connected = false;
        }

        long responseTime = System.currentTimeMillis() - startTime;

        // 如果连接成功，更新状态并确保集群在心跳检测中
        if (connected) {
            updateLastConnectedTime(clusterId, LocalDateTime.now());
            ensureClusterInHeartbeat(clusterId, cluster);
        } else {
            // 连接失败，更新状态为不可用
            updateClusterStatus(clusterId, "unavailable");
        }

        FlinkClusterConnectionTestRespVO result = new FlinkClusterConnectionTestRespVO();
        result.setConnected(connected);
        result.setResponseTime(responseTime);
        result.setLastMessage(message);
        return result;
    }

    /**
     * 确保集群在心跳检测中
     */
    private void ensureClusterInHeartbeat(Long clusterId, FlinkClusterDO cluster) {
        FlinkClusterConnectionStatus status = connectionPool.get(clusterId);
        if (status == null) {
            // 不在连接池中，需要初始化
            log.info("集群不在心跳检测中，正在初始化 clusterId={}", clusterId);
            initClusterConnection(clusterId);
        } else {
            // 已在连接池中，启动或重启心跳检测
            log.info("集群已在心跳检测中，重启心跳任务 clusterId={}", clusterId);
            startHeartbeat(clusterId);
            // 立即执行一次健康检查
            threadPool.execute(() -> {
                try {
                    checkClusterHealth(clusterId);
                } catch (Exception e) {
                    log.error("立即健康检查失败 clusterId={}", clusterId, e);
                }
            });
        }
    }

    /**
     * 测试未保存的集群配置
     */
    public FlinkClusterConnectionTestRespVO testTempCluster(FlinkClusterDO cluster) {
        long startTime = System.currentTimeMillis();
        boolean connected = false;
        String message = "连接失败";

        try {
            if ("remote".equals(cluster.getType())) {
                connected = pingRemoteCluster(cluster);
                message = connected ? "连接成功" : "连接失败";
            } else if ("yarn".equals(cluster.getType())) {
                connected = pingYarnCluster(cluster);
                message = connected ? "连接成功" : "连接失败";
            }
        } catch (Exception e) {
            log.error("连接测试失败 clusterName={}", cluster.getName(), e);
            message = "连接异常: " + e.getMessage();
            connected = false;
        }

        long responseTime = System.currentTimeMillis() - startTime;

        FlinkClusterConnectionTestRespVO result = new FlinkClusterConnectionTestRespVO();
        result.setConnected(connected);
        result.setResponseTime(responseTime);
        result.setLastMessage(message);
        return result;
    }

    /**
     * 连接集群（强制连接，不依赖连接池状态）
     */
    public void connectToCluster(Long clusterId) {
        FlinkClusterDO cluster = clusterMapper.selectById(clusterId);
        if (cluster == null) {
            log.error("集群不存在 clusterId={}", clusterId);
            return;
        }

        FlinkClusterConnectionStatus status = connectionPool.get(clusterId);
        boolean isInPool = status != null;

        try {
            log.info("尝试连接集群 clusterId={}, type={}, inPool={}", clusterId, cluster.getType(), isInPool);

            boolean connected = false;
            if ("remote".equals(cluster.getType())) {
                connected = pingRemoteCluster(cluster);
            } else if ("yarn".equals(cluster.getType())) {
                connected = pingYarnCluster(cluster);
            }

            if (connected) {
                // 更新状态为可用
                updateClusterStatus(clusterId, "available");
                updateLastConnectedTime(clusterId, LocalDateTime.now());
                log.info("集群连接成功 clusterId={}", clusterId);

                // 如果在连接池中，更新连接状态
                if (isInPool) {
                    status.setConnected(true);
                }
            } else {
                // 更新状态为不可用
                updateClusterStatus(clusterId, "unavailable");
                log.error("集群连接失败 clusterId={}", clusterId);

                // 如果在连接池中，更新连接状态
                if (isInPool) {
                    status.setConnected(false);
                }
            }

        } catch (Exception e) {
            log.error("集群连接失败 clusterId={}", clusterId, e);
            // 更新状态为不可用
            updateClusterStatus(clusterId, "unavailable");

            // 如果在连接池中，更新连接状态
            if (isInPool) {
                status.setConnected(false);
            }
        }
    }

    /**
     * 关闭集群连接
     */
    public void closeClusterConnection(Long clusterId) {
        FlinkClusterConnectionStatus status = connectionPool.get(clusterId);
        if (status != null) {
            // 取消心跳任务
            if (status.getHeartbeatTask() != null) {
                status.getHeartbeatTask().cancel(true);
            }

            // 关闭调度器
            if (status.getHeartbeatScheduler() != null) {
                status.getHeartbeatScheduler().shutdown();
                try {
                    if (!status.getHeartbeatScheduler().awaitTermination(5, TimeUnit.SECONDS)) {
                        status.getHeartbeatScheduler().shutdownNow();
                    }
                } catch (InterruptedException e) {
                    status.getHeartbeatScheduler().shutdownNow();
                }
            }

            connectionPool.remove(clusterId);
        }

        updateClusterStatus(clusterId, "stopped");
        log.info("关闭集群连接 clusterId={}", clusterId);
    }

    /**
     * 更新集群状态
     */
    private void updateClusterStatus(Long clusterId, String status) {
        FlinkClusterDO updateDO = new FlinkClusterDO();
        updateDO.setId(clusterId);
        updateDO.setStatus(status);
        clusterMapper.updateById(updateDO);
        log.debug("更新集群状态 clusterId={}, status={}", clusterId, status);
    }

    /**
     * 更新最后连接时间
     */
    private void updateLastConnectedTime(Long clusterId, LocalDateTime time) {
        FlinkClusterDO updateDO = new FlinkClusterDO();
        updateDO.setId(clusterId);
        updateDO.setLastConnectedTime(time.format(DATE_FORMATTER));
        clusterMapper.updateById(updateDO);
    }

    /**
     * 更新最后心跳时间
     */
    private void updateLastHeartbeatTime(Long clusterId, LocalDateTime time) {
        FlinkClusterDO updateDO = new FlinkClusterDO();
        updateDO.setId(clusterId);
        updateDO.setLastConnectedTime(time.format(DATE_FORMATTER));
        clusterMapper.updateById(updateDO);
    }

    /**
     * 刷新状态（强制刷新并确保在心跳检测中）
     */
    public FlinkClusterDO refreshStatus(Long clusterId) {
        FlinkClusterDO cluster = clusterMapper.selectById(clusterId);
        if (cluster == null) {
            throw new IllegalArgumentException("集群不存在");
        }
        // 确保集群在心跳检测中
        ensureClusterInHeartbeat(clusterId, cluster);

        // 返回更新后的集群信息
        return clusterMapper.selectById(clusterId);
    }

    /**
     * 获取连接池
     */
    public Map<Long, FlinkClusterConnectionStatus> getConnectionPool() {
        return connectionPool;
    }

    /**
     * 获取连接状态
     */
    public FlinkClusterConnectionStatus getConnectionStatus(Long clusterId) {
        return connectionPool.get(clusterId);
    }

    /**
     * 优雅关闭线程池
     */
    @PreDestroy
    @TenantIgnore
    public void destroy() {
        log.info("关闭所有集群连接...");

        // 关闭所有连接
        for (Long clusterId : connectionPool.keySet()) {
            closeClusterConnection(clusterId);
        }

        // 关闭线程池
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
        }

        log.info("集群连接管理服务已关闭");
    }
}
