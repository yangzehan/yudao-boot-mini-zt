package cn.iocoder.yudao.module.flink.deploy;

import cn.iocoder.yudao.module.flink.common.deployer.DeployParam;
import cn.iocoder.yudao.module.flink.common.deployer.FlinkJobRemoteDeployer;
import cn.iocoder.yudao.module.flink.common.dto.JobDeployRespDto;
import cn.iocoder.yudao.module.flink.deploy.base.AbstractFlinkJobDyploy;
import cn.iocoder.yudao.module.flink.deploy.param.DeployRemoteJarParam;
import cn.iocoder.yudao.module.flink.deploy.param.DeployRemoteSqlParam;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.streaming.api.environment.RemoteStreamEnvironment;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * Flink作业远程执行器
 *
 * <p>负责将作业提交到远程Flink集群并执行
 *
 * @author yzh
 */
@Slf4j
public class FlinkJobRemoteDeployerImpl extends AbstractFlinkJobDyploy
    implements FlinkJobRemoteDeployer {

  @Override
  public JobDeployRespDto deployJar(DeployParam deployParam) {
    DeployRemoteJarParam remoteParam = validateParam(deployParam, DeployRemoteJarParam.class);
    // 实现ExecuteJarParam接口
    DeployJarParam jarParam =
        new DeployJarParam() {
          @Override
          public String getJarFile() {
            return remoteParam.getJarFile();
          }

          @Override
          public String getEntryPointClassName() {
            return remoteParam.getEntryPointClassName();
          }

          @Override
          public String[] getArgument() {
            return remoteParam.getArgument();
          }

          @Override
          public String getDeployMode() {
            return "remote";
          }

          @Override
          public String getJobName() {
            return remoteParam.getJobName();
          }
        };

    return submitJarTemplate(jarParam, remoteParam.getConfiguration());
  }

  @Override
  public void cancelJob(String jobId, Map<String, String> config) {
    cancelJobLocalAndRemote(config, jobId);
  }

  @Override
  public JobDeployRespDto deploySql(DeployParam deployParam) {
    DeployRemoteSqlParam remoteParam = validateParam(deployParam, DeployRemoteSqlParam.class);
    Configuration configuration = remoteParam.getConfiguration();
    StreamExecutionEnvironment remoteEnvironment =
        RemoteStreamEnvironment.createRemoteEnvironment(
            configuration.get(RestOptions.ADDRESS), configuration.get(RestOptions.PORT));
    // 实现ExecuteSqlParam接口
    DeploySqlParam sqlParam =
        new DeploySqlParam() {
          @Override
          public String getSql() {
            return remoteParam.getSql();
          }

          @Override
          public String getDeployMode() {
            return "remote";
          }

          @Override
          public String getJobName() {
            return remoteParam.getJobName();
          }
        };
    return executeSqlTemplate(sqlParam, remoteEnvironment);
  }
}
