package cn.iocoder.yudao.module.flink.deploy;

import static cn.iocoder.yudao.module.flink.deploy.util.sql.SqlUtil.processScripts;
import static cn.iocoder.yudao.module.flink.deploy.util.yarn.Utils.getYarnConfiguration;
import static org.apache.flink.streaming.api.environment.ExecutionCheckpointingOptions.CHECKPOINTING_INTERVAL;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.util.spring.SpringUtils;
import cn.iocoder.yudao.module.flink.common.deployer.DeployParam;
import cn.iocoder.yudao.module.flink.common.deployer.FlinkJobDeployer;
import cn.iocoder.yudao.module.flink.common.dto.JobDeployRespDto;
import cn.iocoder.yudao.module.flink.deploy.base.AbstractFlinkJobDyploy;
import cn.iocoder.yudao.module.flink.deploy.param.DeployYarnDataIngestionParam;
import cn.iocoder.yudao.module.flink.deploy.param.DeployYarnJarParam;
import cn.iocoder.yudao.module.flink.deploy.param.DeployYarnSqlParam;
import cn.iocoder.yudao.module.flink.deploy.util.yarn.YarnClusterDescriptor;
import cn.iocoder.yudao.module.flink.job.RpcJobStatusHook;
import java.io.File;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.JobID;
import org.apache.flink.client.deployment.ClusterSpecification;
import org.apache.flink.client.deployment.executors.PipelineExecutorUtils;
import org.apache.flink.client.program.*;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.CoreOptions;
import org.apache.flink.configuration.PipelineOptions;
import org.apache.flink.runtime.jobgraph.JobGraph;
import org.apache.flink.runtime.jobgraph.SavepointRestoreSettings;
import org.apache.flink.runtime.rest.messages.*;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.flink.streaming.api.environment.LocalStreamEnvironment;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.graph.StreamGraph;
import org.apache.flink.table.api.bridge.java.StreamStatementSet;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.api.internal.TableEnvironmentImpl;
import org.apache.flink.yarn.YarnClientYarnClusterInformationRetriever;
import org.apache.flink.yarn.YarnClusterClientFactory;
import org.apache.flink.yarn.entrypoint.YarnJobClusterEntrypoint;
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
  private static final String APPLICATION_MAIN_CLASS = "org.apache.flink.cdc.cli.CliExecutor";

  @Override
  public JobDeployRespDto deploySql(DeployParam deployParam) {
    DeployYarnSqlParam yarnSqlParam = validateParam(deployParam, DeployYarnSqlParam.class);
    log.info("开始 flink on yarn 作业部署");
    Configuration configuration = yarnSqlParam.getConfiguration();
    if (ObjUtil.isNull(configuration)) {
      configuration = new Configuration();
    }

    // 配置远程 Flink lib 目录（从 HDFS 加载，节省上传带宽）
    if (yarnSqlParam.getRemoteLibDirs() != null && !yarnSqlParam.getRemoteLibDirs().isEmpty()) {
      String remoteLibDirs = String.join(",", yarnSqlParam.getRemoteLibDirs());
      configuration.setString("yarn.provided.lib.dirs", remoteLibDirs);
      log.info("配置远程 Flink lib 目录: {}", remoteLibDirs);
    }

    LocalStreamEnvironment environment =
        StreamExecutionEnvironment.createLocalEnvironment(configuration);

    // 启用检查点配置（配置已通过Configuration传递）
    if (configuration.contains(CHECKPOINTING_INTERVAL)) {
      long checkpointInterval = configuration.get(CHECKPOINTING_INTERVAL).toMillis();
      environment.enableCheckpointing(checkpointInterval);
    }
    // 设置 parallelism（配置已通过 Configuration 传递）
    if (configuration.contains(CoreOptions.DEFAULT_PARALLELISM)) {
      int parallelism = configuration.get(CoreOptions.DEFAULT_PARALLELISM);
      environment.setParallelism(parallelism);
    } else {
      environment.setParallelism(1); // 默认1
    }
    StreamTableEnvironment stbEnv = StreamTableEnvironment.create(environment);
    StreamStatementSet statementSet = stbEnv.createStatementSet();
    TableEnvironmentImpl tbEnv = (TableEnvironmentImpl) stbEnv;
    processScripts(yarnSqlParam.getSql(), tbEnv, statementSet);
    log.info("生成JobGraph");
    StreamGraph streamGraph = environment.getStreamGraph(true);
    streamGraph.setJobName(yarnSqlParam.getJobName());
    log.info("注册作业状态监控 Hook");
    streamGraph.registerJobStatusHook(
        new RpcJobStatusHook(
            SpringUtils.getProperty("spring.cloud.nacos.discovery.server-addr"),
            SpringUtils.getProperty("spring.cloud.nacos.discovery.namespace")));
    final JobGraph jobGraph;
    try {
      jobGraph =
          PipelineExecutorUtils.getJobGraph(
              streamGraph, configuration, this.getClass().getClassLoader());
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
    log.info("完成jobGraph构建");
    YarnClusterDescriptor clusterDescriptor =
        getClusterDescriptor(
            configuration,
            yarnSqlParam.getYarnSitePath(),
            yarnSqlParam.getHdfsSitePath(),
            yarnSqlParam.getCoreSitePath());

    ClusterSpecification clusterSpecification =
        new YarnClusterClientFactory().getClusterSpecification(configuration);
    try {
      ClusterClientProvider<ApplicationId> flinkApplicationCluster =
          clusterDescriptor.deployInternal(
              clusterSpecification,
              "Flink per-job cluster",
              YarnJobClusterEntrypoint.class.getName(),
              jobGraph,
              true);
      ClusterClient<ApplicationId> clusterClient = flinkApplicationCluster.getClusterClient();
      Map<String, String> config = clusterClient.getFlinkConfiguration().toMap();
      String trackingUrl =
          clusterDescriptor
              .getYarnClient()
              .getApplicationReport(clusterClient.getClusterId())
              .getTrackingUrl();
      config.put("webUiUrl", trackingUrl);
      JobDeployRespDto respDto =
          new JobDeployRespDto()
              .setJobId(jobGraph.getJobID().toString())
              .setJobName(jobGraph.getName())
              .setWebInterfaceUrl(trackingUrl)
              .setConfig(config)
              .setSubmitStatus(true)
              .setSubmitTime(LocalDateTimeUtil.now())
              .setDeployMode("yarn-application")
              .setMessage("部署成功")
              .setFlinkClusterId(String.valueOf(clusterClient.getClusterId()));

      return respDto;

    } catch (Exception e) {
      log.error("部署失败", e);
    }
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
    configuration.set(PipelineOptions.NAME, yarnJarParam.getJobName());
    JobGraph jobGraph = null;
    // 获取 parallelism 配置，默认1
    int parallelism = 1;
    if (configuration.contains(CoreOptions.DEFAULT_PARALLELISM)) {
      parallelism = configuration.get(CoreOptions.DEFAULT_PARALLELISM);
    }
    try {
      jobGraph = PackagedProgramUtils.createJobGraph(program, configuration, parallelism, false);
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
              "Flink per-job cluster",
              YarnJobClusterEntrypoint.class.getName(),
              jobGraph,
              true);
      ClusterClient<ApplicationId> clusterClient = flinkApplicationCluster.getClusterClient();
      Map<String, String> config = clusterClient.getFlinkConfiguration().toMap();
      String trackingUrl =
          clusterDescriptor
              .getYarnClient()
              .getApplicationReport(clusterClient.getClusterId())
              .getTrackingUrl();
      config.put("webUiUrl", trackingUrl);
      JobDeployRespDto respDto =
          new JobDeployRespDto()
              .setJobId(jobGraph.getJobID().toString())
              .setJobName(jobGraph.getName())
              .setWebInterfaceUrl(trackingUrl)
              .setConfig(config)
              .setSubmitStatus(true)
              .setSubmitTime(LocalDateTimeUtil.now())
              .setDeployMode("yarn-application")
              .setMessage("部署成功")
              .setFlinkClusterId(String.valueOf(clusterClient.getClusterId()));

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
  public void cancelJob(String jobId, Map<String, String> config) {
    String webUiUrl = config.get("webUiUrl");

    YarnCancelJobTerminationHeaders jobTerminationHeaders =
        YarnCancelJobTerminationHeaders.getInstance();
    JobCancellationMessageParameters params =
        new JobCancellationMessageParameters()
            .resolveJobId(JobID.fromHexString(jobId))
            .resolveTerminationMode(TerminationModeQueryParameter.TerminationMode.CANCEL);
    String endpointURL = jobTerminationHeaders.getTargetRestEndpointURL();
    webUiUrl = webUiUrl.substring(0, webUiUrl.lastIndexOf("/"));
    String url = MessageParameters.resolveUrl(webUiUrl + endpointURL, params);
    String resp = HttpUtil.get(url, 30000);

    if (!"{}".equals(resp)) {

      throw ServiceExceptionUtil.exception(new ErrorCode(9999, "取消任务错误{}"), resp);
    }
  }

  @Override
  public JobDeployRespDto deployDataIngestion(DeployParam deployParam) {
    log.info("开始部署flink on yarn application 模式数据集成作业");
    DeployYarnDataIngestionParam deployYarnParam =
        validateParam(deployParam, DeployYarnDataIngestionParam.class);
    // 需要验证PipelineOptions.JARS是否已经存在值
    Configuration configuration = deployYarnParam.getConfiguration();
    if (configuration.get(PipelineOptions.JARS) == null) {
      throw ServiceExceptionUtil.exception(new ErrorCode(9999, "没有定义CDC dist jar包"));
    }
    // 配置远程 Flink lib 目录（从 HDFS 加载，节省上传带宽）
    if (deployYarnParam.getRemoteLibDirs() != null
        && !deployYarnParam.getRemoteLibDirs().isEmpty()) {
      String remoteLibDirs = String.join(",", deployYarnParam.getRemoteLibDirs());
      configuration.setString("yarn.provided.lib.dirs", remoteLibDirs);
      log.info("配置远程 Flink lib 目录: {}", remoteLibDirs);
    }
    configuration.set(PipelineOptions.NAME, deployYarnParam.getJobName());
    log.info("构建 cdc jobGraph");
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    PackagedProgram program = null;
    // 获取 parallelism 配置，默认1
    int parallelism = 1;
    if (configuration.contains(CoreOptions.DEFAULT_PARALLELISM)) {
      parallelism = configuration.get(CoreOptions.DEFAULT_PARALLELISM);
    }
    try {
      program =
          PackagedProgram.newBuilder()
              .setJarFile(new File(deployYarnParam.getCdcDistJarPath()))
              .setEntryPointClassName(APPLICATION_MAIN_CLASS)
              .setArguments(mapper.readTree(deployYarnParam.getContent()).toString())
              .setSavepointRestoreSettings(SavepointRestoreSettings.none())
              .build();
      JobGraph jobGraph =
          PackagedProgramUtils.createJobGraph(program, configuration, parallelism, false);
      jobGraph.setJobStatusHooks(
          Collections.singletonList(
              new RpcJobStatusHook(
                  SpringUtils.getProperty("spring.cloud.nacos.discovery.server-addr"),
                  SpringUtils.getProperty("spring.cloud.nacos.discovery.namespace"))));

      log.info("构建 cdc jobGraph成功");
      YarnClusterDescriptor clusterDescriptor =
          getClusterDescriptor(
              configuration,
              deployYarnParam.getYarnSitePath(),
              deployYarnParam.getHdfsSitePath(),
              deployYarnParam.getCoreSitePath());

      ClusterSpecification clusterSpecification =
          new YarnClusterClientFactory().getClusterSpecification(configuration);

      ClusterClientProvider<ApplicationId> flinkApplicationCluster =
          clusterDescriptor.deployInternal(
              clusterSpecification,
              "Flink per-job cluster",
              YarnJobClusterEntrypoint.class.getName(),
              jobGraph,
              true);
      ClusterClient<ApplicationId> clusterClient = flinkApplicationCluster.getClusterClient();
      Map<String, String> config = clusterClient.getFlinkConfiguration().toMap();
      String trackingUrl =
          clusterDescriptor
              .getYarnClient()
              .getApplicationReport(clusterClient.getClusterId())
              .getTrackingUrl();
      config.put("webUiUrl", trackingUrl);
      JobDeployRespDto respDto =
          new JobDeployRespDto()
              .setJobId(jobGraph.getJobID().toString())
              .setJobName(jobGraph.getName())
              .setWebInterfaceUrl(trackingUrl)
              .setConfig(config)
              .setSubmitStatus(true)
              .setSubmitTime(LocalDateTimeUtil.now())
              .setDeployMode("yarn-application")
              .setMessage("部署成功")
              .setFlinkClusterId(String.valueOf(clusterClient.getClusterId()));
      return respDto;
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      if (ObjUtil.isNotNull(program)) {
        program.close();
      }
    }
  }
}
