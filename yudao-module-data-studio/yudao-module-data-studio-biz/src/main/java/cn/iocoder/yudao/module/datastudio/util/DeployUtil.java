package cn.iocoder.yudao.module.datastudio.util;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.module.datastudio.controller.admin.job.vo.JobDeployReqVO;
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
    flinkConfig.setDeployMode(reqVO.getDeployMode());
    flinkConfig.setClusterId(reqVO.getClusterId());
    flinkConfig.setFlinkVersion(reqVO.getFlinkVersion());
    flinkConfig.setParallelism(reqVO.getParallelism());
    flinkConfig.setCheckpointInterval(reqVO.getCheckpointInterval());
    FlinkClusterDO flinkCluster = this.flinkClusterService.getFlinkCluster(reqVO.getClusterId());
    HashMap<String, String> exConfig = new HashMap<>();

    switch (reqVO.getDeployMode()) {
      case "local":
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
            exConfig, "yarn.provided.usrlib.dir", flinkCluster.getYarnProvidedUsrLibDir());
        putMapIfNotNull(
            exConfig,
            "jobmanager.memory.process.size",
            flinkCluster.getJobmanagerMemoryProcessSize() + "mb");
        exConfig.put("yarn.flink-dist-jar", flinkCluster.getYarnFlinkDistJar());
        exConfig.put(
            "taskmanager.memory.process.size",
            flinkCluster.getTaskmanagerMemoryProcessSize() + "mb");
        exConfig.put(
            "taskmanager.numberOfTaskSlots",
            flinkCluster.getTaskmanagerNumberOfTaskSlots().toString());
        if (ObjUtil.isNull(flinkConfig.getExtendedConfig())) {
          flinkConfig.setExtendedConfig(exConfig);
        } else {
          flinkConfig.getExtendedConfig().putAll(exConfig);
        }
        break;
      case "reemote":
        String[] s = flinkCluster.getRemoteUrl().split(":");

        exConfig.put("rest.address", s[0]);
        exConfig.put("rest.port", s[1]);
        flinkConfig.setExtendedConfig(exConfig);
        break;
    }

    return flinkConfig;
  }

  public Map<String, String> putMapIfNotNull(Map<String, String> map, String key, Object value) {
    if (value != null) {
      map.put(key, String.valueOf(value));
    }
    return map;
  }
}
