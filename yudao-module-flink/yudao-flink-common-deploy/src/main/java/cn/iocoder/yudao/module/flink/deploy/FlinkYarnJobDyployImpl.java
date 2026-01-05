package cn.iocoder.yudao.module.flink.deploy;

import static cn.iocoder.yudao.module.flink.deploy.util.Utils.getYarnConfiguration;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.util.spring.SpringUtils;
import cn.iocoder.yudao.module.flink.common.deployer.DeployParam;
import cn.iocoder.yudao.module.flink.common.deployer.FlinkJobDeployer;
import cn.iocoder.yudao.module.flink.common.dto.JobDeployRespDto;
import cn.iocoder.yudao.module.flink.deploy.base.AbstractFlinkJobDyploy;
import cn.iocoder.yudao.module.flink.deploy.param.DeployYarnJarParam;
import cn.iocoder.yudao.module.flink.deploy.util.YarnClusterDescriptor;
import cn.iocoder.yudao.module.flink.job.RpcJobStatusHook;
import java.io.File;
import java.util.Collections;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.client.deployment.ClusterSpecification;
import org.apache.flink.client.deployment.application.ApplicationConfiguration;
import org.apache.flink.client.program.*;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.PipelineOptions;
import org.apache.flink.runtime.jobgraph.JobGraph;
import org.apache.flink.runtime.jobgraph.SavepointRestoreSettings;
import org.apache.flink.yarn.YarnClientYarnClusterInformationRetriever;
import org.apache.flink.yarn.YarnClusterClientFactory;
import org.apache.flink.yarn.entrypoint.YarnApplicationClusterEntryPoint;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.springframework.lang.NonNull;

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
    if (ObjUtil.isNull(configuration)) {
      configuration = new Configuration();
    }
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
    log.info("设置有效配置信息");
    // 设置作业名称

    configuration.set(PipelineOptions.NAME, yarnJarParam.getJobName());
    configuration.set(
        PipelineOptions.JARS,
        Collections.singletonList(new File(yarnJarParam.getJarFile()).toURI().toString()));

    // 配置远程 Flink lib 目录（从 HDFS 加载，节省上传带宽）
    if (yarnJarParam.getRemoteLibDirs() != null && !yarnJarParam.getRemoteLibDirs().isEmpty()) {
      String remoteLibDirs = String.join(",", yarnJarParam.getRemoteLibDirs());
      configuration.setString("yarn.provided.lib.dirs", remoteLibDirs);
      log.info("配置远程 Flink lib 目录: {}", remoteLibDirs);
    }
    log.info("生成JobGraph");
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

    YarnClusterDescriptor clusterDescriptor =
        getClusterDescriptor(
            configuration,
            yarnJarParam.getYarnSitePath(),
            yarnJarParam.getHdfsSitePath(),
            yarnJarParam.getCoreSitePath());

    ClusterSpecification clusterSpecification =
        new YarnClusterClientFactory().getClusterSpecification(configuration);
    try {
      ClusterClientProvider<ApplicationId> flinkApplicationCluster =
          clusterDescriptor.deployInternal(
              clusterSpecification,
              "Flink Application Cluster",
              YarnApplicationClusterEntryPoint.class.getName(),
              jobGraph,
              false);
      ClusterClient<ApplicationId> clusterClient = flinkApplicationCluster.getClusterClient();
      JobDeployRespDto respDto =
          new JobDeployRespDto()
              .setJobId(jobGraph.getJobID().toString())
              .setJobName(jobGraph.getName())
              .setWebInterfaceUrl(clusterClient.getWebInterfaceURL())
              .setConfig(clusterClient.getFlinkConfiguration().toMap())
              .setSubmitStatus(true)
              .setSubmitTime(LocalDateTimeUtil.now())
              .setDeployMode("yarn-application")
              .setMessage("部署成功");

      return respDto;

    } catch (Exception e) {
      log.error("部署失败", e);
    }
    return null;
  }

  public YarnClusterDescriptor getClusterDescriptor(
      Configuration configuration,
      @NonNull String yarnSitePath,
      @NonNull String hdfsSitePath,
      @NonNull String coreSitePath) {
    {
      final YarnClient yarnClient = YarnClient.createYarnClient();
      final YarnConfiguration yarnConfig = getYarnConfiguration(configuration);

      File yarnSiteFile = new File(yarnSitePath);
      File hdfsSiteFile = new File(hdfsSitePath);
      File coreSiteFile = new File(coreSitePath);
      yarnConfig.addResource(new Path(hdfsSiteFile.getAbsolutePath()));
      yarnConfig.addResource(new Path(coreSiteFile.getAbsolutePath()));
      yarnConfig.addResource(new Path(yarnSiteFile.getAbsolutePath()));

      yarnClient.init(yarnConfig);
      yarnClient.start();

      return new YarnClusterDescriptor(
          configuration,
          yarnConfig,
          yarnClient,
          YarnClientYarnClusterInformationRetriever.create(yarnClient),
          false);
    }
  }

  @Override
  public void cancelJob(String jobId, Map<String, String> config) {}
}
