package cn.iocoder.yudao.module.flink.deploy;

import static org.apache.flink.streaming.api.environment.ExecutionCheckpointingOptions.CHECKPOINTING_INTERVAL;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.iocoder.yudao.module.flink.common.deployer.DeployParam;
import cn.iocoder.yudao.module.flink.common.deployer.FlinkJobDeployer;
import cn.iocoder.yudao.module.flink.common.dto.JobDeployRespDto;
import cn.iocoder.yudao.module.flink.deploy.base.AbstractFlinkJobDyploy;
import cn.iocoder.yudao.module.flink.deploy.enums.DeployModeEnum;
import cn.iocoder.yudao.module.flink.deploy.param.DeployRemoteDataIngestionParam;
import cn.iocoder.yudao.module.flink.deploy.param.DeployRemoteJarParam;
import cn.iocoder.yudao.module.flink.deploy.param.DeployRemoteSqlParam;
import java.util.Collections;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.cdc.cli.parser.YamlPipelineDefinitionParser;
import org.apache.flink.cdc.composer.PipelineExecution;
import org.apache.flink.cdc.composer.definition.PipelineDef;
import org.apache.flink.cdc.composer.flink.FlinkPipelineComposer;
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
  public JobDeployRespDto deployDataIngestion(DeployParam deployParam) {
    log.info("开始执行远程数据集成任务");
    DeployRemoteDataIngestionParam deployRemoteParam =
        validateParam(deployParam, DeployRemoteDataIngestionParam.class);
    Configuration configuration = deployRemoteParam.getConfiguration();
    FlinkPipelineComposer composer =
        FlinkPipelineComposer.ofRemoteCluster(configuration, Collections.emptyList());
    JobDeployRespDto respDto = new JobDeployRespDto();
    try {
      PipelineDef pipelineDef =
          new YamlPipelineDefinitionParser()
              .parse(
                  deployRemoteParam.getContent(),
                  new org.apache.flink.cdc.common.configuration.Configuration());
      PipelineExecution.ExecutionInfo executionInfo = composer.compose(pipelineDef).execute();

      respDto
          .setSubmitTime(LocalDateTimeUtil.now())
          .setSubmitStatus(true)
          .setJobId(executionInfo.getId())
          .setDeployMode(DeployModeEnum.REMOTE.getDeployName())
          .setMessage("部署成功")
          .setConfig(configuration.toMap())
          .setWebInterfaceUrl(
              "http://"
                  + configuration.getString(RestOptions.ADDRESS)
                  + ":"
                  + configuration.getInteger(RestOptions.PORT));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return respDto;
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
    return executeSqlTemplate(sqlParam, remoteEnvironment);
  }
}
