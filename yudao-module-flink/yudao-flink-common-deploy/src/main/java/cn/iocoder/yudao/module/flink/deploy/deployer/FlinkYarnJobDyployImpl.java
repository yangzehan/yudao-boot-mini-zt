package cn.iocoder.yudao.module.flink.deploy.deployer;

import static cn.iocoder.yudao.module.flink.deploy.enums.FlinkDeployErrorCodeConstants.*;
import static cn.iocoder.yudao.module.flink.deploy.util.yarn.ClusterDescriptorUtil.getClusterDescriptor;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.module.flink.common.deployer.DeployParam;
import cn.iocoder.yudao.module.flink.common.deployer.FlinkJobDeployer;
import cn.iocoder.yudao.module.flink.common.dto.JobDeployRespDto;
import cn.iocoder.yudao.module.flink.common.enums.JobTypeEnum;
import cn.iocoder.yudao.module.flink.deploy.base.AbstractFlinkJobDyploy;
import cn.iocoder.yudao.module.flink.deploy.base.FlinkApplicationExecutor;
import cn.iocoder.yudao.module.flink.deploy.constant.NacosConstant;
import cn.iocoder.yudao.module.flink.deploy.param.*;
import cn.iocoder.yudao.module.flink.deploy.util.yarn.ClusterDescriptorUtil;
import cn.iocoder.yudao.module.flink.deploy.util.yarn.YarnClusterDescriptor;
import java.util.Collections;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.JobID;
import org.apache.flink.client.deployment.ClusterSpecification;
import org.apache.flink.client.deployment.application.ApplicationConfiguration;
import org.apache.flink.client.program.ClusterClient;
import org.apache.flink.client.program.ClusterClientProvider;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.PipelineOptions;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.runtime.messages.webmonitor.JobDetails;
import org.apache.flink.runtime.messages.webmonitor.MultipleJobsDetails;
import org.apache.flink.runtime.rest.messages.*;
import org.apache.flink.runtime.rest.util.RestMapperUtils;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.flink.yarn.YarnClusterClientFactory;
import org.apache.flink.yarn.entrypoint.YarnApplicationClusterEntryPoint;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.apache.hadoop.yarn.client.api.YarnClient;

/**
 * @author yzh
 */
@Slf4j
public class FlinkYarnJobDyployImpl extends AbstractFlinkJobDyploy implements FlinkJobDeployer {
  private static final String APPLICATION_MAIN_CLASS = "org.apache.flink.cdc.cli.CliExecutor";

  private static JobDetails getJobDetails(
      String trackingUrl,
      ApplicationReport applicationReport,
      YarnClient yarnClient,
      int retryCount)
      throws JsonProcessingException {
    if (retryCount > 5) {
      throw ServiceExceptionUtil.exception(
          YARN_JOB_DETAILS_FETCH_FAILED, "重试次数已达上限(" + retryCount + ")");
    }
    retryCount++;
    String content = HttpUtil.get(trackingUrl + JobsOverviewHeaders.URL);
    if (content == null) {
      throw ServiceExceptionUtil.exception(YARN_JOB_DETAILS_FETCH_FAILED, "HTTP响应为空");
    }
    MultipleJobsDetails multipleJobsDetails =
        RestMapperUtils.getStrictObjectMapper().readValue(content, MultipleJobsDetails.class);
    if (multipleJobsDetails.getJobs().isEmpty()) {
      try {
        applicationReport = yarnClient.getApplicationReport(applicationReport.getApplicationId());
        if (applicationReport.getYarnApplicationState().equals(YarnApplicationState.RUNNING)) {
          Thread.sleep(1000L);
          // 递归调用并返回结果
          return getJobDetails(trackingUrl, applicationReport, yarnClient, retryCount);
        }
      } catch (Exception e) {
        log.error("获取作业详情失败", e);
        // 抛出异常而不是继续执行
        throw ServiceExceptionUtil.exception(YARN_JOB_DETAILS_FETCH_FAILED, e);
      }
      // 如果不是RUNNING状态，抛出异常
      throw ServiceExceptionUtil.exception(YARN_APPLICATION_NOT_RUNNING);
    }

    return CollectionUtil.get(multipleJobsDetails.getJobs(), 0);
  }

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
    log.info("构建ApplicationConfiguration");
    configuration.set(
        PipelineOptions.JARS,
        Collections.singletonList(
            "hdfs://localhost:9000/flink/flink1.18/app/yudao-flink-common-deploy-2025.10-jdk8-SNAPSHOT.jar"));
    String[] programArguments = new String[4];
    DeployLocalSqlParam param = new DeployLocalSqlParam();
    param.setSql(yarnSqlParam.getSql());
    param.setJobName(yarnSqlParam.getJobName());
    param.setConfiguration(null);
    programArguments[0] = JSONUtil.toJsonStr(param);
    programArguments[1] = JobTypeEnum.FLINK_SQL.name();
    programArguments[2] = NacosConstant.getDiscoveryServerAddr();
    programArguments[3] = NacosConstant.getDiscoveryNamespace();
    new ApplicationConfiguration(programArguments, FlinkApplicationExecutor.class.getName())
        .applyToConfiguration(configuration);
    YarnClusterDescriptor clusterDescriptor =
        getClusterDescriptor(
            configuration,
            yarnSqlParam.getYarnSitePath(),
            yarnSqlParam.getHdfsSitePath(),
            yarnSqlParam.getCoreSitePath());
    log.info("构建ApplicationConfiguration完成");

    ClusterSpecification clusterSpecification =
        new YarnClusterClientFactory().getClusterSpecification(configuration);

    try {
      ClusterClientProvider<ApplicationId> flinkApplicationCluster =
          clusterDescriptor.deployInternal(
              clusterSpecification,
              "Flink application cluster",
              YarnApplicationClusterEntryPoint.class.getName(),
              null,
              true);

      ClusterClient<ApplicationId> clusterClient = flinkApplicationCluster.getClusterClient();

      Map<String, String> config = clusterClient.getFlinkConfiguration().toMap();
      JobDeployRespDto respDto = new JobDeployRespDto();

      ApplicationReport applicationReport =
          clusterDescriptor.getYarnClient().getApplicationReport(clusterClient.getClusterId());
      String trackingUrl = applicationReport.getTrackingUrl();

      JobDetails jobDetails =
          getJobDetails(trackingUrl, applicationReport, clusterDescriptor.getYarnClient(), 0);

      config.put("webUiUrl", trackingUrl);

      respDto
          .setJobId(jobDetails.getJobId().toString())
          .setJobName(jobDetails.getJobName())
          .setWebInterfaceUrl(trackingUrl)
          .setConfig(config)
          .setSubmitStatus(true)
          .setSubmitTime(LocalDateTimeUtil.now())
          .setDeployMode("yarn-application")
          .setMessage("部署成功")
          .setFlinkClusterId(String.valueOf(clusterClient.getClusterId()));

      return respDto;

    } catch (Exception e) {
      log.error(
          "YARN SQL 部署失败 - 作业名: {}, 远程Lib目录: {}",
          yarnSqlParam.getJobName(),
          yarnSqlParam.getRemoteLibDirs(),
          e);
      throw ServiceExceptionUtil.exception(YARN_DEPLOY_FAILED, e);
    }
  }

  @Override
  public JobDeployRespDto deployJar(DeployParam deployParam, boolean async) {
    DeployYarnJarParam yarnJarParam = validateParam(deployParam, DeployYarnJarParam.class);
    log.info("开始 flink on yarn 作业部署，使用 FlinkApplicationExecutor");
    Configuration configuration = yarnJarParam.getConfiguration();
    if (ObjUtil.isNull(configuration)) {
      configuration = new Configuration();
    }

    // 配置远程 Flink lib 目录（从 HDFS 加载，节省上传带宽）
    if (yarnJarParam.getRemoteLibDirs() != null && !yarnJarParam.getRemoteLibDirs().isEmpty()) {
      String remoteLibDirs = String.join(",", yarnJarParam.getRemoteLibDirs());
      configuration.setString("yarn.provided.lib.dirs", remoteLibDirs);
      log.info("配置远程 Flink lib 目录: {}", remoteLibDirs);
    }

    // 构建 DeployLocalJarParam 参数
    DeployRemoteJarParam remoteJarParam = new DeployRemoteJarParam();
    remoteJarParam.setConfiguration(configuration);
    remoteJarParam.setEntryPointClassName(yarnJarParam.getEntryPointClassName());
    remoteJarParam.setArgument(yarnJarParam.getArgument());
    remoteJarParam.setJobName(yarnJarParam.getJobName());
    Path tureJarPath = new Path(yarnJarParam.getJarFile());
    remoteJarParam.setJarFile(tureJarPath.getName());
    // 将参数序列化为 JSON
    String programArgJson = JSONUtil.toJsonStr(remoteJarParam);

    // 构建 ApplicationConfiguration
    String[] programArguments = new String[4];
    programArguments[0] = programArgJson;
    programArguments[1] = JobTypeEnum.JAR.name();
    programArguments[2] = NacosConstant.getDiscoveryServerAddr();
    programArguments[3] = NacosConstant.getDiscoveryNamespace();
    // 配置作业名称
    configuration.set(PipelineOptions.NAME, yarnJarParam.getJobName());

    // 配置 PipelineOptions.JARS（用于本地打包）

    configuration.set(
        PipelineOptions.JARS,
        Collections.singletonList(
            "hdfs://localhost:9000/flink/flink1.18/app/yudao-flink-common-deploy-2025.10-jdk8-SNAPSHOT.jar"));

    // 应用 ApplicationConfiguration
    new ApplicationConfiguration(programArguments, FlinkApplicationExecutor.class.getName())
        .applyToConfiguration(configuration);

    configuration.set(RestOptions.BIND_PORT, "40001");

    YarnClusterDescriptor clusterDescriptor =
        getClusterDescriptor(
            configuration,
            yarnJarParam.getYarnSitePath(),
            yarnJarParam.getHdfsSitePath(),
            yarnJarParam.getCoreSitePath());

    ClusterSpecification clusterSpecification =
        new YarnClusterClientFactory().getClusterSpecification(configuration);

    try {
      // 使用 YarnJobClusterEntrypoint 配合 FlinkApplicationExecutor 提交作业

      clusterDescriptor.addUserJar(tureJarPath);

      ClusterDescriptorUtil.Result result =
          ClusterDescriptorUtil.deployYarnApplication(clusterDescriptor, clusterSpecification);

      JobDeployRespDto respDto =
          new JobDeployRespDto()
              .setJobId(result.jobDetails.getJobId().toString())
              .setJobName(result.jobDetails.getJobName())
              .setWebInterfaceUrl(result.trackingUrl)
              .setConfig(result.config)
              .setSubmitStatus(true)
              .setSubmitTime(LocalDateTimeUtil.now())
              .setDeployMode("yarn-application")
              .setMessage("部署成功")
              .setFlinkClusterId(String.valueOf(result.clusterClient.getClusterId()));

      return respDto;

    } catch (Exception e) {
      log.error(
          "YARN JAR 部署失败 - 作业名: {}, JAR文件: {}",
          yarnJarParam.getJobName(),
          yarnJarParam.getJarFile(),
          e);
      throw ServiceExceptionUtil.exception(YARN_DEPLOY_FAILED, e);
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
      log.error("取消任务错误 - 作业ID: {}, 响应: {}", jobId, resp);
      throw ServiceExceptionUtil.exception(JOB_CANCEL_FAILED, resp);
    }
  }

  @Override
  public JobDeployRespDto deployDataIngestion(DeployParam deployParam) {
    DeployYarnDataIngestionParam yarnParam =
        validateParam(deployParam, DeployYarnDataIngestionParam.class);
    DataIngestionDeployer dataIngestionDeployer = new DataIngestionDeployerImpl();
    return dataIngestionDeployer.deployYarn(yarnParam);
  }
}
