package cn.iocoder.yudao.module.datastudio.framework.flink.cluster;

import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.flinkcluster.FlinkClusterDO;
import cn.iocoder.yudao.module.datastudio.dal.mysql.flinkcluster.FlinkClusterMapper;
import cn.iocoder.yudao.module.datastudio.service.flinkcluster.FlinkClusterConnectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.util.List;

/**
 * 集群连接初始化配置
 */
@Slf4j
@Configuration
public class FlinkClusterConnectionInitializer {

    @Autowired
    private FlinkClusterMapper clusterMapper;

    @Autowired
    private FlinkClusterConnectionService connectionService;

    /**
     * 应用启动后初始化所有活跃集群的连接
     */
    @EventListener(ApplicationReadyEvent.class)
    @TenantIgnore
    public void initClusterConnections() {
        log.info("开始初始化Flink集群连接...");

        try {
            // 查询所有活跃的集群
            List<FlinkClusterDO> activeClusters = clusterMapper.selectList(
                new LambdaQueryWrapper<FlinkClusterDO>()
                    .in(FlinkClusterDO::getStatus, "running", "available", "unavailable")
            );

            log.info("发现 {} 个活跃集群", activeClusters.size());

            for (FlinkClusterDO cluster : activeClusters) {
                try {
                    connectionService.initClusterConnection(cluster.getId());
                    log.info("初始化集群连接成功 clusterId={}, name={}",
                            cluster.getId(), cluster.getName());
                } catch (Exception e) {
                    log.error("初始化集群连接失败 clusterId={}, name={}",
                            cluster.getId(), cluster.getName(), e);
                }
            }

            log.info("集群连接初始化完成，共初始化 {} 个集群", activeClusters.size());

        } catch (Exception e) {
            log.error("初始化集群连接异常", e);
        }
    }
}
