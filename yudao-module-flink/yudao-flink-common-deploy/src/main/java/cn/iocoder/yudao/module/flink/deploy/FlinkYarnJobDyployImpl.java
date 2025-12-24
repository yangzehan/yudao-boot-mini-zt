package cn.iocoder.yudao.module.flink.deploy;

import cn.iocoder.yudao.framework.common.util.spring.SpringUtils;
import cn.iocoder.yudao.module.flink.common.deployer.DeployParam;
import cn.iocoder.yudao.module.flink.common.deployer.FlinkJobDeployer;
import cn.iocoder.yudao.module.flink.common.dto.JobDeployRespDto;
import cn.iocoder.yudao.module.flink.deploy.base.AbstractFlinkJobDyploy;
import cn.iocoder.yudao.module.flink.deploy.param.DeployYarnJarParam;
import cn.iocoder.yudao.module.flink.deploy.util.Utils;
import cn.iocoder.yudao.module.flink.deploy.util.YarnClusterDescriptor;
import cn.iocoder.yudao.module.flink.job.RpcJobStatusHook;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.client.deployment.ClusterSpecification;
import org.apache.flink.client.deployment.application.ApplicationConfiguration;
import org.apache.flink.client.program.PackagedProgram;
import org.apache.flink.client.program.PackagedProgramUtils;
import org.apache.flink.client.program.ProgramInvocationException;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.PipelineOptions;
import org.apache.flink.runtime.jobgraph.JobGraph;
import org.apache.flink.runtime.jobgraph.SavepointRestoreSettings;
import org.apache.flink.util.Preconditions;
import org.apache.flink.yarn.YarnClientYarnClusterInformationRetriever;
import org.apache.flink.yarn.YarnClusterClientFactory;
import org.apache.flink.yarn.entrypoint.YarnApplicationClusterEntryPoint;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.conf.YarnConfiguration;

/**
 * @author yzh
 */
@Slf4j
public class FlinkYarnJobDyployImpl extends AbstractFlinkJobDyploy implements FlinkJobDeployer {
  @Override
  public JobDeployRespDto deploySql(DeployParam deployParam) {
    return null;
  }

  @Override
  public JobDeployRespDto deployJar(DeployParam deployParam) {
    DeployYarnJarParam yarnJarParam = validateParam(deployParam, DeployYarnJarParam.class);

    log.info("开始 flink on yarn 作业部署");
    Configuration configuration = yarnJarParam.getConfiguration();
    final ApplicationConfiguration applicationConfiguration =
        new ApplicationConfiguration(new String[0], "cn");

    PackagedProgram program = null;
    try {
      program =
          PackagedProgram.newBuilder()
              .setJarFile(new File(yarnJarParam.getJarFile()))
              .setEntryPointClassName(yarnJarParam.getEntryPointClassName())
              .setArguments(yarnJarParam.getArgument())
              .setSavepointRestoreSettings(SavepointRestoreSettings.none())
              .build();
    } catch (ProgramInvocationException e) {
      throw new RuntimeException(e);
    }
    log.info("生成JobGraph");
    configuration.set(PipelineOptions.NAME, yarnJarParam.getJobName());
    JobGraph jobGraph = null;
    try {
      jobGraph = PackagedProgramUtils.createJobGraph(program, configuration, 1, false);
    } catch (ProgramInvocationException e) {
      throw new RuntimeException(e);
    }
    log.info("注册作业状态监控 Hook");
    jobGraph.setJobStatusHooks(
        Collections.singletonList(
            new RpcJobStatusHook(
                SpringUtils.getProperty("spring.cloud.nacos.discovery.server-addr"),
                SpringUtils.getProperty("spring.cloud.nacos.discovery.namespace"))));
    log.info("完成jobGraph构建");

    YarnClusterDescriptor clusterDescriptor = getClusterDescriptor(configuration);

    applicationConfiguration.applyToConfiguration(configuration);

    // No need to do pipelineJars validation if it is a PyFlink job.
    if (!(PackagedProgramUtils.isPython(applicationConfiguration.getApplicationClassName())
        || PackagedProgramUtils.isPython(applicationConfiguration.getProgramArguments()))) {
      final List<String> pipelineJars =
          configuration.getOptional(PipelineOptions.JARS).orElse(Collections.emptyList());
      Preconditions.checkArgument(pipelineJars.size() == 1, "Should only have one jar");
    }
    ClusterSpecification clusterSpecification =
        new YarnClusterClientFactory().getClusterSpecification(configuration);
    try {
      clusterDescriptor.deployInternal(
          clusterSpecification,
          "Flink Application Cluster",
          YarnApplicationClusterEntryPoint.class.getName(),
          null,
          false);
    } catch (Exception e) {
      log.error("部署失败");
    }
    return null;
  }

  public YarnClusterDescriptor getClusterDescriptor(Configuration configuration) {
    {
      final YarnClient yarnClient = YarnClient.createYarnClient();
      final YarnConfiguration yarnConfiguration =
          Utils.getYarnAndHadoopConfiguration(configuration);

      if (System.getenv().get("IN_TESTS") != null) {
        File f = new File(System.getenv("YARN_CONF_DIR"), Utils.YARN_SITE_FILE_NAME);
        Path yarnSitePath = new Path(f.getAbsolutePath());
        yarnConfiguration.addResource(yarnSitePath);
      }

      yarnClient.init(yarnConfiguration);
      yarnClient.start();

      return new YarnClusterDescriptor(
          configuration,
          yarnConfiguration,
          yarnClient,
          YarnClientYarnClusterInformationRetriever.create(yarnClient),
          false);
    }
  }

  @Override
  public void cancelJob(String jobId, Map<String, String> config) {}
}
