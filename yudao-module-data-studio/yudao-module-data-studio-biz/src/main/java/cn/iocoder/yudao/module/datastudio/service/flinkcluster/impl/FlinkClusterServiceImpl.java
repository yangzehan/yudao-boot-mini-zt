package cn.iocoder.yudao.module.datastudio.service.flinkcluster.impl;

import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.datastudio.controller.admin.flinkcluster.vo.extra.FlinkClusterConnectionTestRespVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.flinkcluster.vo.extra.FlinkClusterStatisticsRespVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.flinkcluster.vo.extra.FlinkClusterTypeRespVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.flinkcluster.vo.extra.FlinkClusterVersionRespVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.flinkcluster.vo.list.FlinkClusterListReqVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.flinkcluster.vo.list.FlinkClusterPageReqVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.flinkcluster.vo.save.FlinkClusterBatchDeleteReqVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.flinkcluster.vo.save.FlinkClusterSaveReqVO;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.flinkcluster.FlinkClusterDO;
import cn.iocoder.yudao.module.datastudio.dal.mysql.flinkcluster.FlinkClusterMapper;
import cn.iocoder.yudao.module.datastudio.service.flinkcluster.FlinkClusterConnectionService;
import cn.iocoder.yudao.module.datastudio.service.flinkcluster.FlinkClusterService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Flink 集群配置 Service 实现
 *
 * @author 芋道源码
 */
@Slf4j
@Service
public class FlinkClusterServiceImpl implements FlinkClusterService {

    @Resource
    private FlinkClusterMapper flinkClusterMapper;

    @Resource
    private FlinkClusterConnectionService connectionService;

    @Override
    public Long createFlinkCluster(FlinkClusterSaveReqVO createReqVO) {
        // 1. 校验参数
        validateClusterParams(createReqVO, false);

        // 2. 构建数据对象
        FlinkClusterDO cluster = BeanUtils.toBean(createReqVO, FlinkClusterDO.class);
        cluster.setCreateTime(LocalDateTime.now());
        cluster.setStatus("stopped"); // 默认状态
        cluster.setCreator(getCurrentUserId()); // TODO: 从上下文中获取当前用户ID

        // 3. 处理 JSON 字段
        if (createReqVO.getTags() != null) {
            cluster.setTags(JsonUtils.toJsonString(createReqVO.getTags()));
        }
        if (createReqVO.getProjects() != null) {
            cluster.setProjects(JsonUtils.toJsonString(createReqVO.getProjects()));
        }
        if (createReqVO.getAlertConfig() != null) {
            cluster.setAlertConfig(createReqVO.getAlertConfig());
        }
        if (createReqVO.getCustomConfig() != null) {
            cluster.setCustomConfig(createReqVO.getCustomConfig());
        }

        // 4. 插入数据库
        flinkClusterMapper.insert(cluster);

        // 然后刷新状态，确保状态更新到数据库
        FlinkClusterDO updatedCluster = connectionService.refreshStatus(cluster.getId());

        if (updatedCluster != null) {
            log.info("集群状态已更新 clusterId={}, status={}", cluster.getId(), updatedCluster.getStatus());
        } else {
            log.warn("集群状态刷新失败 clusterId={}", cluster.getId());
        }

        return cluster.getId();
    }

    /**
     * 获取线程池（用于异步任务）
     */
    private final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
            5,
            10,
            60L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(100),
            r -> new Thread(r, "flink-cluster-create-test"),
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    @Override
    public void updateFlinkCluster(FlinkClusterSaveReqVO updateReqVO) {
        // 1. 校验参数
        validateClusterParams(updateReqVO, true);

        // 2. 获取旧数据
        FlinkClusterDO oldCluster = flinkClusterMapper.selectById(updateReqVO.getId());
        if (oldCluster == null) {
            throw ServiceExceptionUtil.exception(GlobalErrorCodeConstants.NOT_FOUND);
        }

        // 3. 远程集群不允许更新
        if ("remote".equals(oldCluster.getType())) {
            throw ServiceExceptionUtil.exception(GlobalErrorCodeConstants.BAD_REQUEST, "远程集群不支持更新操作");
        }

        // 4. 构建更新数据
        FlinkClusterDO cluster = BeanUtils.toBean(updateReqVO, FlinkClusterDO.class);

        // 5. 处理 JSON 字段
        if (updateReqVO.getTags() != null) {
            cluster.setTags(JsonUtils.toJsonString(updateReqVO.getTags()));
        }
        if (updateReqVO.getProjects() != null) {
            cluster.setProjects(JsonUtils.toJsonString(updateReqVO.getProjects()));
        }
        if (updateReqVO.getAlertConfig() != null) {
            cluster.setAlertConfig(updateReqVO.getAlertConfig());
        }
        if (updateReqVO.getCustomConfig() != null) {
            cluster.setCustomConfig(updateReqVO.getCustomConfig());
        }

        // 6. 更新数据库
        flinkClusterMapper.updateById(cluster);

        // 7. 重新初始化连接（如果需要）
        if ("yarn".equals(cluster.getType())) {
            connectionService.closeClusterConnection(cluster.getId());
            if ("running".equals(cluster.getStatus()) || "available".equals(cluster.getStatus())) {
                connectionService.initClusterConnection(cluster.getId());
            }
        }
    }

    @Override
    public void deleteFlinkCluster(Long id) {
        FlinkClusterDO cluster = flinkClusterMapper.selectById(id);
        if (cluster == null) {
            return;
        }

        // TODO: 检查是否有运行中的作业，如果有则不允许删除或需要确认

        // 关闭连接
        connectionService.closeClusterConnection(id);

        // 删除数据库记录
        flinkClusterMapper.deleteById(id);
    }

    @Override
    public void deleteFlinkClusterList(FlinkClusterBatchDeleteReqVO deleteReqVO) {
        for (Long id : deleteReqVO.getIds()) {
            deleteFlinkCluster(id);
        }
    }

    @Override
    public FlinkClusterDO getFlinkCluster(Long id) {
        return flinkClusterMapper.selectById(id);
    }

    @Override
    public List<FlinkClusterDO> getFlinkClusterList(FlinkClusterListReqVO listReqVO) {
        LambdaQueryWrapper<FlinkClusterDO> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(listReqVO.getType())) {
            wrapper.eq(FlinkClusterDO::getType, listReqVO.getType());
        }
        if (StringUtils.hasText(listReqVO.getStatus())) {
            wrapper.eq(FlinkClusterDO::getStatus, listReqVO.getStatus());
        }
        if (StringUtils.hasText(listReqVO.getKeyword())) {
            wrapper.and(w -> w
                    .like(FlinkClusterDO::getName, listReqVO.getKeyword())
                    .or()
                    .like(FlinkClusterDO::getDescription, listReqVO.getKeyword())
            );
        }

        wrapper.orderByDesc(FlinkClusterDO::getId);
        return flinkClusterMapper.selectList(wrapper);
    }

    @Override
    public PageResult<FlinkClusterDO> getFlinkClusterPage(FlinkClusterPageReqVO pageReqVO) {
        LambdaQueryWrapper<FlinkClusterDO> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(pageReqVO.getType())) {
            wrapper.eq(FlinkClusterDO::getType, pageReqVO.getType());
        }
        if (StringUtils.hasText(pageReqVO.getStatus())) {
            wrapper.eq(FlinkClusterDO::getStatus, pageReqVO.getStatus());
        }
        if (StringUtils.hasText(pageReqVO.getKeyword())) {
            wrapper.and(w -> w
                    .like(FlinkClusterDO::getName, pageReqVO.getKeyword())
                    .or()
                    .like(FlinkClusterDO::getDescription, pageReqVO.getKeyword())
            );
        }

        wrapper.orderByDesc(FlinkClusterDO::getId);
        return flinkClusterMapper.selectPage(pageReqVO, wrapper);
    }

    @Override
    public FlinkClusterConnectionTestRespVO testConnection(Long id) {
        // 使用连接管理服务进行测试
        return connectionService.testConnection(id);
    }

    @Override
    public FlinkClusterConnectionTestRespVO testConnection(FlinkClusterSaveReqVO clusterReqVO) {
        // 构建临时集群DO（不保存到数据库）
        FlinkClusterDO cluster = BeanUtils.toBean(clusterReqVO, FlinkClusterDO.class);

        // 使用连接服务测试临时集群配置
        return connectionService.testTempCluster(cluster);
    }

    @Override
    public FlinkClusterDO refreshStatus(Long id) {
        // 使用连接管理服务刷新状态
        return connectionService.refreshStatus(id);
    }

    @Override
    public List<FlinkClusterTypeRespVO> getClusterTypes() {
        List<FlinkClusterTypeRespVO> types = new ArrayList<>();

        FlinkClusterTypeRespVO remoteType = new FlinkClusterTypeRespVO();
        remoteType.setValue("remote");
        remoteType.setLabel("远程集群");
        remoteType.setEditable(false);
        types.add(remoteType);

        FlinkClusterTypeRespVO yarnType = new FlinkClusterTypeRespVO();
        yarnType.setValue("yarn");
        yarnType.setLabel("Flink on Yarn");
        yarnType.setEditable(true);
        types.add(yarnType);

        return types;
    }

    @Override
    public List<FlinkClusterVersionRespVO> getFlinkVersions() {
        String[] versions = {"1.12", "1.13", "1.14", "1.15", "1.16", "1.17", "1.18", "1.19"};

        return java.util.Arrays.stream(versions)
                .map(version -> {
                    FlinkClusterVersionRespVO vo = new FlinkClusterVersionRespVO();
                    vo.setVersion(version);
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public FlinkClusterStatisticsRespVO getStatistics() {
        List<FlinkClusterDO> allClusters = flinkClusterMapper.selectList(null);

        FlinkClusterStatisticsRespVO stats = new FlinkClusterStatisticsRespVO();
        stats.setTotal(allClusters.size());

        // 按类型统计
        long remoteCount = allClusters.stream().filter(c -> "remote".equals(c.getType())).count();
        long yarnCount = allClusters.stream().filter(c -> "yarn".equals(c.getType())).count();
        stats.setRemoteCount((int) remoteCount);
        stats.setYarnCount((int) yarnCount);

        // 按状态统计
        long runningCount = allClusters.stream().filter(c -> "running".equals(c.getStatus())).count();
        long stoppedCount = allClusters.stream().filter(c -> "stopped".equals(c.getStatus())).count();
        long availableCount = allClusters.stream().filter(c -> "available".equals(c.getStatus())).count();
        long unavailableCount = allClusters.stream().filter(c -> "unavailable".equals(c.getStatus())).count();

        stats.setRunningCount((int) runningCount);
        stats.setStoppedCount((int) stoppedCount);
        stats.setAvailableCount((int) availableCount);
        stats.setUnavailableCount((int) unavailableCount);

        // 按项目统计
        Map<String, Integer> byProject = new HashMap<>();
        for (FlinkClusterDO cluster : allClusters) {
            if (StringUtils.hasText(cluster.getProjects())) {
                try {
                    List<String> projects = JsonUtils.parseArray(cluster.getProjects(), String.class);
                    if (projects != null) {
                        for (String project : projects) {
                            byProject.merge(project, 1, Integer::sum);
                        }
                    }
                } catch (Exception e) {
                    log.warn("解析项目列表失败", e);
                }
            }
        }
        stats.setByProject(byProject);

        return stats;
    }

    /**
     * 验证集群参数
     */
    private void validateClusterParams(FlinkClusterSaveReqVO reqVO, boolean isUpdate) {
        if (isUpdate && reqVO.getId() == null) {
            throw ServiceExceptionUtil.exception(GlobalErrorCodeConstants.BAD_REQUEST, "集群ID不能为空");
        }

        if (!StringUtils.hasText(reqVO.getName())) {
            throw ServiceExceptionUtil.exception(GlobalErrorCodeConstants.BAD_REQUEST, "集群名称不能为空");
        }

        if (reqVO.getName().length() < 2 || reqVO.getName().length() > 50) {
            throw ServiceExceptionUtil.exception(GlobalErrorCodeConstants.BAD_REQUEST, "集群名称长度必须在 2 到 50 个字符");
        }

        if (!StringUtils.hasText(reqVO.getType())) {
            throw ServiceExceptionUtil.exception(GlobalErrorCodeConstants.BAD_REQUEST, "集群类型不能为空");
        }

        // 根据集群类型验证必填字段
        if ("remote".equals(reqVO.getType())) {
            if (!StringUtils.hasText(reqVO.getRemoteUrl())) {
                throw ServiceExceptionUtil.exception(GlobalErrorCodeConstants.BAD_REQUEST, "远程集群地址不能为空");
            }
        } else if ("yarn".equals(reqVO.getType())) {
            if (!StringUtils.hasText(reqVO.getQueueName())) {
                throw ServiceExceptionUtil.exception(GlobalErrorCodeConstants.BAD_REQUEST, "YARN队列名称不能为空");
            }
            if (!StringUtils.hasText(reqVO.getYarnSitePath())) {
                throw ServiceExceptionUtil.exception(GlobalErrorCodeConstants.BAD_REQUEST, "Yarn配置文件路径不能为空");
            }
            if (!StringUtils.hasText(reqVO.getHdfsSitePath())) {
                throw ServiceExceptionUtil.exception(GlobalErrorCodeConstants.BAD_REQUEST, "HDFS配置文件路径不能为空");
            }
            if (!StringUtils.hasText(reqVO.getCoreSitePath())) {
                throw ServiceExceptionUtil.exception(GlobalErrorCodeConstants.BAD_REQUEST, "Core配置文件路径不能为空");
            }
        } else {
            throw ServiceExceptionUtil.exception(GlobalErrorCodeConstants.BAD_REQUEST, "不支持的集群类型");
        }
    }

    /**
     * 测试集群连接（实际实现）
     */
    private Boolean testClusterConnection(FlinkClusterDO cluster) {
        // TODO: 实际实现连接测试逻辑
        // 这里是一个示例，实际需要根据不同类型进行不同的连接测试

        try {
            // 模拟连接测试
            Thread.sleep(100); // 模拟网络延迟

            // 根据不同类型进行不同的连接测试
            if ("remote".equals(cluster.getType())) {
                // 测试远程集群：连接到JobManager的REST API
                String remoteUrl = cluster.getRemoteUrl();
                if (!StringUtils.hasText(remoteUrl)) {
                    return false;
                }
                // TODO: 实际实现HTTP请求测试
                // HttpUtil.get(remoteUrl + "/jobmanager/config")
                return true;

            } else if ("yarn".equals(cluster.getType())) {
                // 测试Yarn集群：通过配置文件路径验证
                String yarnSitePath = cluster.getYarnSitePath();
                String hdfsSitePath = cluster.getHdfsSitePath();
                String coreSitePath = cluster.getCoreSitePath();
                if (!StringUtils.hasText(yarnSitePath)
                        || !StringUtils.hasText(hdfsSitePath)
                        || !StringUtils.hasText(coreSitePath)) {
                    return false;
                }
                // TODO: 实际实现HTTP请求测试
                return true;
            }

            return false;
        } catch (Exception e) {
            log.error("连接测试异常", e);
            return false;
        }
    }

    /**
     * 获取当前用户ID（TODO: 实际从上下文中获取）
     */
    private String getCurrentUserId() {
        // TODO: 从 SecurityContext 中获取当前用户ID
        return "admin";
    }

    /**
     * 优雅关闭线程池
     */
    private void shutdownThreadPool() {
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
        }
    }

    /**
     * 优雅关闭线程池
     */
    @javax.annotation.PreDestroy
    public void destroy() {
        shutdownThreadPool();
    }

}
