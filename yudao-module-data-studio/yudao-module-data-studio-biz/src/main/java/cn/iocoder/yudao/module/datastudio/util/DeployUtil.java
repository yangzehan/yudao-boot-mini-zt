package cn.iocoder.yudao.module.datastudio.util;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.module.datastudio.controller.admin.job.vo.JobDeployReqVO;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.dataIngestion.DataIngestionConfigDO;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.dataIngestion.DataIngestionDO;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.file.SqlEditConfigDO;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.file.SqlEditDO;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.flinkcluster.FlinkClusterDO;
import cn.iocoder.yudao.module.datastudio.service.flinkcluster.FlinkClusterService;
import cn.iocoder.yudao.module.flink.common.dto.FlinkConfig;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * @author yzh
 */
@Component
public class DeployUtil {
  @Resource private FlinkClusterService flinkClusterService;

  public FlinkConfig getFlinkConfigByJobDeployReqVO(JobDeployReqVO reqVO) {
    FlinkConfig flinkConfig = new FlinkConfig();
    String deployMode = reqVO.getDeployMode();
    flinkConfig.setDeployMode(deployMode);
    flinkConfig.setClusterId(reqVO.getClusterId());
    flinkConfig.setFlinkVersion(reqVO.getFlinkVersion());
    flinkConfig.setParallelism(reqVO.getParallelism());
    flinkConfig.setCheckpointInterval(reqVO.getCheckpointInterval());
    FlinkClusterDO flinkCluster = this.flinkClusterService.getFlinkCluster(reqVO.getClusterId());
    // todo 添加默认配置
    return buildFlinkConfig(deployMode, flinkConfig, flinkCluster);
  }

  private FlinkConfig buildFlinkConfig(
      String deployMode, FlinkConfig flinkConfig, FlinkClusterDO flinkCluster) {
    HashMap<String, String> exConfig = new HashMap<>();
    Map<String, String> config = flinkConfig.getExtendedConfig();
    switch (deployMode) {
      case "local":
        if (CollectionUtil.isEmpty(config)) {
          exConfig.put("rest.address", "localhost");
          exConfig.put("rest.bind-port", "3000-10000");
          exConfig.put("execution.target", "local");
          flinkConfig.setExtendedConfig(exConfig);
        } else {
          config.put("execution.target", "local");
        }
        break;
      case "yarn-session":
        break;
      case "yarn-per-job":
        break;
      case "yarn-application":
        flinkConfig.setYarnSitePath(flinkCluster.getYarnSitePath());
        flinkConfig.setHdfsSitePath(flinkCluster.getHdfsSitePath());
        flinkConfig.setCoreSitePath(flinkCluster.getCoreSitePath());
        putMapIfNotNull(exConfig, "yarn.provided.lib.dirs", flinkCluster.getYarnProvidedLibDirs());
        putMapIfNotNull(
            exConfig,
            "$internal.application.main",
            "cn.iocoder.yudao.module.flink.deploy.base.FlinkApplicationExecutor");
        putMapIfNotNull(
            exConfig, "yarn.provided.usrlib.dir", flinkCluster.getYarnProvidedUsrLibDir());
        putMapIfNotNull(
            exConfig,
            "jobmanager.memory.process.size",
            flinkCluster.getJobmanagerMemoryProcessSize() + "mb");
        exConfig.put("yarn.flink-dist-jar", flinkCluster.getYarnFlinkDistJar());
        exConfig.put("$internal.yarn.log-config-file", flinkCluster.getYarnAppLogConfigPath());
        exConfig.put(
            "taskmanager.memory.process.size",
            flinkCluster.getTaskmanagerMemoryProcessSize() + "mb");
        exConfig.put(
            "taskmanager.numberOfTaskSlots",
            flinkCluster.getTaskmanagerNumberOfTaskSlots().toString());
        if (ObjUtil.isNull(config)) {
          flinkConfig.setExtendedConfig(exConfig);
        } else {
          config.putAll(exConfig);
        }
        break;
      case "remote":
        String[] s = flinkCluster.getRemoteUrl().split(":");

        exConfig.put("rest.address", s[0]);
        exConfig.put("rest.port", s[1]);
        flinkConfig.setExtendedConfig(exConfig);
        break;
    }

    return flinkConfig;
  }

  public FlinkConfig getFlinkConfigBySqlEditDOAndSqlEditConfigDO(
      SqlEditDO sqlEditDO, SqlEditConfigDO sqlEditConfigDO) {

    FlinkConfig config = sqlEditConfigDO.getConfig();
    Long clusterId = config.getClusterId();
    FlinkClusterDO cluster = flinkClusterService.getFlinkCluster(clusterId);
    String deployMode = config.getDeployMode();
    return buildFlinkConfig(deployMode, config, cluster);
  }

  public FlinkConfig getFlinkConfigByDataIngestionDOAndDataIngestionConfigDO(
      DataIngestionDO dataIngestionDO, DataIngestionConfigDO dataIngestionConfigDO) {

    FlinkConfig config = dataIngestionConfigDO.getConfig();
    Long clusterId = config.getClusterId();
    FlinkClusterDO cluster = flinkClusterService.getFlinkCluster(clusterId);
    String deployMode = config.getDeployMode();
    return buildFlinkConfig(deployMode, config, cluster);
  }

  public Map<String, String> putMapIfNotNull(Map<String, String> map, String key, Object value) {
    if (value != null) {
      map.put(key, String.valueOf(value));
    }
    return map;
  }
}
