package cn.iocoder.yudao.module.flink.common.deployer;

import cn.iocoder.yudao.module.flink.common.dto.JobDeployRespDto;
import java.util.Map;

/**
 * @author yzh
 */
public interface FlinkJobDeployer {
  /**
   * 执行SQL作业
   *
   * @param deployParam 部署参数
   * @return JobDeployRespDto 包含作业ID、集群信息、Web界面URL等
   */
  JobDeployRespDto deploySql(DeployParam deployParam);

  /**
   * 执行JAR作业
   *
   * @param deployParam 部署参数
   * @return JobDeployRespDto 包含作业ID、集群信息、Web界面URL等
   */
  JobDeployRespDto deployJar(DeployParam deployParam);

  void cancelJob(String jobId, Map<String, String> config);

  JobDeployRespDto deployDataIngestion(DeployParam deployParam);
}
