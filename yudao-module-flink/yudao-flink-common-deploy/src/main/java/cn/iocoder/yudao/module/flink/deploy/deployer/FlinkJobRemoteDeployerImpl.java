package cn.iocoder.yudao.module.flink.deploy.deployer;

import static cn.iocoder.yudao.module.flink.deploy.enums.FlinkDeployErrorCodeConstants.*;
import static org.apache.flink.streaming.api.environment.ExecutionCheckpointingOptions.CHECKPOINTING_INTERVAL;

import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.module.flink.common.deployer.DeployParam;
import cn.iocoder.yudao.module.flink.common.deployer.FlinkJobDeployer;
import cn.iocoder.yudao.module.flink.common.dto.JobDeployRespDto;
import cn.iocoder.yudao.module.flink.deploy.base.AbstractFlinkJobDyploy;
import cn.iocoder.yudao.module.flink.deploy.param.DeployRemoteDataIngestionParam;
import cn.iocoder.yudao.module.flink.deploy.param.DeployRemoteJarParam;
import cn.iocoder.yudao.module.flink.deploy.param.DeployRemoteSqlParam;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.CoreOptions;
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
public class FlinkJobRemoteDeployerImpl extends AbstractFlinkJobDyploy implements FlinkJobDeployer {

  @Override
  public JobDeployRespDto deployJar(DeployParam deployParam, boolean async) {
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

    try {
      return submitJarTemplate(jarParam, remoteParam.getConfiguration());
    } catch (Exception e) {
      log.error("远程 JAR 部署失败 - 作业名: {}, 地址: {}:{}",
              remoteParam.getJobName(),
              remoteParam.getConfiguration().get(RestOptions.ADDRESS),
              remoteParam.getConfiguration().get(RestOptions.PORT), e);
      throw ServiceExceptionUtil.exception(JOB_SUBMIT_FAILED, e);
    }
  }

  @Override
  public void cancelJob(String jobId, Map<String, String> config) {
    cancelJobLocalAndRemote(config, jobId);
  }

  @Override
  public JobDeployRespDto deployDataIngestion(DeployParam deployParam) {
    DeployRemoteDataIngestionParam remoteParam =
        validateParam(deployParam, DeployRemoteDataIngestionParam.class);
    DataIngestionDeployer dataIngestionDeployer = new DataIngestionDeployerImpl();
    return dataIngestionDeployer.deployRemote(remoteParam);
  }

  @Override
  public JobDeployRespDto deploySql(DeployParam deployParam) {
    DeployRemoteSqlParam remoteParam = validateParam(deployParam, DeployRemoteSqlParam.class);
    Configuration configuration = remoteParam.getConfiguration();
    StreamExecutionEnvironment remoteEnvironment =
        RemoteStreamEnvironment.createRemoteEnvironment(
            configuration.get(RestOptions.ADDRESS), configuration.get(RestOptions.PORT));

    // 启用检查点配置（配置已通过Configuration传递）
    if (configuration.contains(CHECKPOINTING_INTERVAL)) {
      long checkpointInterval = configuration.get(CHECKPOINTING_INTERVAL).toMillis();
      remoteEnvironment.enableCheckpointing(checkpointInterval);
    } else {
      remoteEnvironment.enableCheckpointing(5000L); // 默认5秒
    }

    // 设置 parallelism（配置已通过 Configuration 传递）
    if (configuration.contains(CoreOptions.DEFAULT_PARALLELISM)) {
      int parallelism = configuration.get(CoreOptions.DEFAULT_PARALLELISM);
      remoteEnvironment.setParallelism(parallelism);
    }

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

    try {
      return executeSqlTemplate(sqlParam, remoteEnvironment);
    } catch (Exception e) {
      log.error("远程 SQL 部署失败 - 作业名: {}, 地址: {}:{}",
              remoteParam.getJobName(),
              remoteParam.getConfiguration().get(RestOptions.ADDRESS),
              remoteParam.getConfiguration().get(RestOptions.PORT), e);
      throw ServiceExceptionUtil.exception(SQL_EXECUTION_FAILED, e);
    }
  }
}
