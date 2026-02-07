package cn.iocoder.yudao.module.flink.deploy.deployer;

import static cn.iocoder.yudao.module.flink.deploy.enums.FlinkDeployErrorCodeConstants.*;
import static cn.iocoder.yudao.module.flink.deploy.util.yarn.ClusterDescriptorUtil.deployYarnApplication;
import static cn.iocoder.yudao.module.flink.deploy.util.yarn.ClusterDescriptorUtil.getClusterDescriptor;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.module.flink.common.dto.JobDeployRespDto;
import cn.iocoder.yudao.module.flink.common.enums.JobTypeEnum;
import cn.iocoder.yudao.module.flink.deploy.base.FlinkApplicationExecutor;
import cn.iocoder.yudao.module.flink.deploy.constant.NacosConstant;
import cn.iocoder.yudao.module.flink.deploy.param.DeployLocalDataIngestionParam;
import cn.iocoder.yudao.module.flink.deploy.param.DeployRemoteDataIngestionParam;
import cn.iocoder.yudao.module.flink.deploy.param.DeployYarnDataIngestionParam;
import cn.iocoder.yudao.module.flink.deploy.service.impl.AsyncTaskServiceImpl;
import cn.iocoder.yudao.module.flink.deploy.util.yarn.ClusterDescriptorUtil;
import cn.iocoder.yudao.module.flink.deploy.util.yarn.YarnClusterDescriptor;
import cn.iocoder.yudao.module.flink.job.RpcJobStatusHook;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.JobExecutionResult;
import org.apache.flink.cdc.cli.parser.YamlPipelineDefinitionParser;
import org.apache.flink.cdc.common.configuration.Configuration;
import org.apache.flink.cdc.common.pipeline.PipelineOptions;
import org.apache.flink.cdc.composer.PipelineExecution;
import org.apache.flink.cdc.composer.definition.PipelineDef;
import org.apache.flink.cdc.composer.flink.FlinkPipelineComposer;
import org.apache.flink.client.deployment.ClusterSpecification;
import org.apache.flink.client.deployment.application.ApplicationConfiguration;
import org.apache.flink.configuration.CoreOptions;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.core.execution.JobClient;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.graph.StreamGraph;
import org.apache.flink.yarn.YarnClusterClientFactory;
import org.apache.hadoop.fs.Path;

/**
 * 数据摄取部署器实现类
 *
 * <p>负责将数据摄取作业部署到不同的运行环境：本地、远程 Flink 集群、YARN
 *
 * @author yzh
 */
@Slf4j
public class DataIngestionDeployerImpl implements DataIngestionDeployer {

  /** Flink CDC PipelineComposer 类名 */
  private static final String FLINK_PIPELINE_COMPOSER_CLASS =
      "org.apache.flink.cdc.composer.flink.FlinkPipelineComposer";

  /**
   * 通过反射创建 FlinkPipelineComposer
   *
   * @param env StreamExecutionEnvironment
   * @param isBlocking 是否阻塞运行
   * @return FlinkPipelineComposer 实例
   */
  private FlinkPipelineComposer createFlinkPipelineComposer(
      StreamExecutionEnvironment env, boolean isBlocking) {
    try {
      Class<FlinkPipelineComposer> clazz =
          (Class<FlinkPipelineComposer>) Class.forName(FLINK_PIPELINE_COMPOSER_CLASS);
      java.lang.reflect.Constructor<FlinkPipelineComposer> constructor =
          clazz.getDeclaredConstructor(StreamExecutionEnvironment.class, boolean.class);
      constructor.setAccessible(true);
      return constructor.newInstance(env, isBlocking);
    } catch (Exception e) {
      throw ServiceExceptionUtil.exception(DATA_INGESTION_PIPELINE_COMPOSER_FAILED, e);
    }
  }

  /**
   * 构建并初始化 JobDeployRespDto
   *
   * @param jobId 作业 ID
   * @param jobName 作业名称
   * @param deployMode 部署模式
   * @param webInterfaceUrl Web 界面地址
   * @param configuration Flink 配置
   * @return 初始化的 JobDeployRespDto
   */
  private JobDeployRespDto buildJobDeployRespDto(
      String jobId,
      String jobName,
      String deployMode,
      String webInterfaceUrl,
      Map<String, String> configuration) {
    return new JobDeployRespDto()
        .setJobId(jobId)
        .setJobName(jobName)
        .setDeployMode(deployMode)
        .setWebInterfaceUrl(webInterfaceUrl)
        .setConfig(configuration)
        .setSubmitStatus(true)
        .setMessage("部署成功")
        .setSubmitTime(LocalDateTimeUtil.now());
  }

  @Override
  public JobDeployRespDto deployLocal(DeployLocalDataIngestionParam param) {
    // 添加jobstatushook
    log.info("开始部署本地数据摄取任务");
    JobDeployRespDto respDto;

    org.apache.flink.configuration.Configuration flinkConfig =
        param.getConfiguration() == null
            ? new org.apache.flink.configuration.Configuration()
            : param.getConfiguration();
    StreamExecutionEnvironment env;
    boolean isBlocking;
    if (ObjUtil.isNotNull(flinkConfig)) {
      // 创建 StreamExecutionEnvironment
      env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(flinkConfig);
      isBlocking = true;
    } else {
      env = StreamExecutionEnvironment.getExecutionEnvironment();
      isBlocking = false;
    }

    try {
      // 使用反射创建 FlinkPipelineComposer
      FlinkPipelineComposer composer = createFlinkPipelineComposer(env, true);
      // 解析 pipeline 定义
      PipelineDef pipelineDef =
          new YamlPipelineDefinitionParser().parse(param.getContent(), new Configuration());
      composer.compose(pipelineDef);
      String jobName = pipelineDef.getConfig().get(PipelineOptions.PIPELINE_NAME);
      PipelineExecution.ExecutionInfo executionInfo;
      StreamGraph streamGraph = env.getStreamGraph();
      streamGraph.registerJobStatusHook(
          new RpcJobStatusHook(
              NacosConstant.getDiscoveryServerAddr(), NacosConstant.getDiscoveryNamespace()));
      if (isBlocking) {
        JobClient jobClient = env.executeAsync(streamGraph);
        new AsyncTaskServiceImpl().monitorEnvByJobClient(jobClient, env);
        executionInfo =
            new PipelineExecution.ExecutionInfo(jobClient.getJobID().toString(), jobName);
      } else {
        JobExecutionResult execute = env.execute(streamGraph);
        executionInfo = new PipelineExecution.ExecutionInfo(execute.getJobID().toString(), jobName);
      }

      String webInterfaceUrl = null;
      if (flinkConfig != null) {
        webInterfaceUrl =
            "http://"
                + flinkConfig.get(RestOptions.ADDRESS)
                + ":"
                + (flinkConfig.get(RestOptions.PORT) == null
                    ? "8081"
                    : String.valueOf(flinkConfig.get(RestOptions.PORT)));
      }

      respDto =
          buildJobDeployRespDto(
              executionInfo.getId(),
              jobName,
              "local",
              webInterfaceUrl,
              flinkConfig == null ? null : flinkConfig.toMap());

    } catch (Exception e) {
      log.error("本地数据摄取部署失败 - 作业配置: {}", param.getJobName(), e);
      throw ServiceExceptionUtil.exception(DATA_INGESTION_LOCAL_DEPLOY_FAILED, e);
    }
    return respDto;
  }

  @Override
  public JobDeployRespDto deployRemote(DeployRemoteDataIngestionParam param) {
    log.info("开始部署远程数据摄取任务");

    org.apache.flink.configuration.Configuration configuration = param.getConfiguration();
    try {
      StreamExecutionEnvironment env = new StreamExecutionEnvironment(configuration);
      // 使用反射创建 FlinkPipelineComposer
      FlinkPipelineComposer composer = createFlinkPipelineComposer(env, false);

      PipelineDef pipelineDef =
          new YamlPipelineDefinitionParser().parse(param.getContent(), new Configuration());
      composer.compose(pipelineDef);

      String jobName = pipelineDef.getConfig().get(PipelineOptions.PIPELINE_NAME);
      StreamGraph streamGraph = env.getStreamGraph();
      streamGraph.registerJobStatusHook(
          new RpcJobStatusHook(
              NacosConstant.getDiscoveryServerAddr(), NacosConstant.getDiscoveryNamespace()));
      JobClient jobClient = env.executeAsync(streamGraph);
      String jobId = jobClient.getJobID().toString();

      String webInterfaceUrl =
          "http://"
              + configuration.getString(RestOptions.ADDRESS)
              + ":"
              + configuration.getInteger(RestOptions.PORT);

      return buildJobDeployRespDto(
          jobId, jobName, "remote", webInterfaceUrl, configuration.toMap());
    } catch (Exception e) {
      log.error("远程数据摄取部署失败 - 作业配置: {}", param.getJobName(), e);
      throw ServiceExceptionUtil.exception(DATA_INGESTION_REMOTE_DEPLOY_FAILED, e);
    }
  }

  @Override
  public JobDeployRespDto deployYarn(DeployYarnDataIngestionParam param) {
    log.info("开始部署 YARN 数据摄取任务");
    org.apache.flink.configuration.Configuration configuration = param.getConfiguration();
    if (ObjUtil.isNull(configuration)) {
      configuration = new org.apache.flink.configuration.Configuration();
    }

    // 配置远程 Flink lib 目录（从 HDFS 加载，节省上传带宽）
    if (param.getRemoteLibDirs() != null && !param.getRemoteLibDirs().isEmpty()) {
      String remoteLibDirs = String.join(",", param.getRemoteLibDirs());
      configuration.setString("yarn.provided.lib.dirs", remoteLibDirs);
      log.info("配置远程 Flink lib 目录: {}", remoteLibDirs);
    }

    // 构建 DeployLocalDataIngestionParam 参数
    DeployLocalDataIngestionParam localParam = new DeployLocalDataIngestionParam();
    localParam.setContent(param.getContent());
    localParam.setJobName(param.getJobName());
    localParam.setClusterId(param.getClusterId());
    localParam.setClusterName(param.getClusterName());
    localParam.setFileId(param.getFileId());
    // 将参数序列化为 JSON
    String programArgJson = JSONUtil.toJsonStr(localParam);

    // 构建 ApplicationConfiguration
    String[] programArguments = new String[4];
    programArguments[0] = programArgJson;
    programArguments[1] = JobTypeEnum.DATA_INGESTION.name();
    programArguments[2] = NacosConstant.getDiscoveryServerAddr();
    programArguments[3] = NacosConstant.getDiscoveryNamespace();

    // 配置作业名称
    configuration.set(org.apache.flink.configuration.PipelineOptions.NAME, param.getJobName());

    // 配置 PipelineOptions.JARS（用于本地打包）
    configuration.set(
        org.apache.flink.configuration.PipelineOptions.JARS,
        java.util.Collections.singletonList(
            "hdfs://localhost:9000/flink/flink1.18/app/yudao-flink-common-deploy-2025.10-jdk8-SNAPSHOT.jar"));
    configuration.set(
        CoreOptions.FLINK_JM_JVM_OPTIONS,
        "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005");
    // 应用 ApplicationConfiguration
    new ApplicationConfiguration(programArguments, FlinkApplicationExecutor.class.getName())
        .applyToConfiguration(configuration);

    YarnClusterDescriptor clusterDescriptor =
        getClusterDescriptor(
            configuration,
            param.getYarnSitePath(),
            param.getHdfsSitePath(),
            param.getCoreSitePath());

    clusterDescriptor.addUserJar(new Path(param.getCdcDistJarPath()));

    ClusterSpecification clusterSpecification =
        new YarnClusterClientFactory().getClusterSpecification(configuration);

    try {
      ClusterDescriptorUtil.Result result =
          deployYarnApplication(clusterDescriptor, clusterSpecification);

      return new JobDeployRespDto()
          .setJobId(result.jobDetails.getJobId().toString())
          .setJobName(result.jobDetails.getJobName())
          .setWebInterfaceUrl(result.trackingUrl)
          .setConfig(result.config)
          .setSubmitStatus(true)
          .setSubmitTime(LocalDateTimeUtil.now())
          .setDeployMode("yarn-application")
          .setMessage("部署成功")
          .setFlinkClusterId(String.valueOf(result.clusterClient.getClusterId()));

    } catch (Exception e) {
      log.error("YARN 数据摄取部署失败 - 作业名: {}, ClusterId: {}",
              param.getJobName(), param.getClusterId(), e);
      throw ServiceExceptionUtil.exception(DATA_INGESTION_YARN_DEPLOY_FAILED, e);
    }
  }
}
