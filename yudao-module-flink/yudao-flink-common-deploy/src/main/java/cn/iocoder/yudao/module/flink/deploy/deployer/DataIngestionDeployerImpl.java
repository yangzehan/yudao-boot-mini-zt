package cn.iocoder.yudao.module.flink.deploy.deployer;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.module.flink.common.dto.JobDeployRespDto;
import cn.iocoder.yudao.module.flink.common.enums.JobTypeEnum;
import cn.iocoder.yudao.module.flink.deploy.base.FlinkApplicationExecutor;
import cn.iocoder.yudao.module.flink.deploy.constant.NacosConstant;
import cn.iocoder.yudao.module.flink.deploy.param.DeployLocalDataIngestionParam;
import cn.iocoder.yudao.module.flink.deploy.param.DeployRemoteDataIngestionParam;
import cn.iocoder.yudao.module.flink.deploy.param.DeployYarnDataIngestionParam;
import cn.iocoder.yudao.module.flink.deploy.util.yarn.YarnClusterDescriptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.client.deployment.ClusterSpecification;
import org.apache.flink.client.deployment.application.ApplicationConfiguration;
import org.apache.flink.client.program.ClusterClient;
import org.apache.flink.client.program.ClusterClientProvider;
import org.apache.flink.cdc.cli.parser.YamlPipelineDefinitionParser;
import org.apache.flink.cdc.common.configuration.Configuration;
import org.apache.flink.cdc.common.pipeline.PipelineOptions;
import org.apache.flink.cdc.composer.PipelineExecution;
import org.apache.flink.cdc.composer.definition.PipelineDef;
import org.apache.flink.cdc.composer.flink.FlinkPipelineComposer;
import org.apache.flink.configuration.CoreOptions;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.runtime.messages.webmonitor.JobDetails;
import org.apache.flink.runtime.messages.webmonitor.MultipleJobsDetails;
import org.apache.flink.runtime.rest.messages.JobsOverviewHeaders;
import org.apache.flink.runtime.rest.util.RestMapperUtils;
import org.apache.flink.yarn.YarnClientYarnClusterInformationRetriever;
import org.apache.flink.yarn.YarnClusterClientFactory;
import org.apache.flink.yarn.entrypoint.YarnApplicationClusterEntryPoint;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.springframework.lang.NonNull;

import java.io.File;
import java.util.Map;

/**
 * 数据摄取部署器实现类
 *
 * <p>负责将数据摄取作业部署到不同的运行环境：本地、远程 Flink 集群、YARN</p>
 *
 * @author yzh
 */
@Slf4j
public class DataIngestionDeployerImpl implements DataIngestionDeployer {

    @Override
    public JobDeployRespDto deployLocal(DeployLocalDataIngestionParam param) {
        log.info("开始部署本地数据摄取任务");
        JobDeployRespDto respDto = new JobDeployRespDto();

        org.apache.flink.configuration.Configuration flinkConfig = param.getConfiguration();
        if (StrUtil.isBlank(flinkConfig.get(RestOptions.ADDRESS))) {
            flinkConfig.set(RestOptions.ADDRESS, "localhost");
        }

        try {
            // 使用反射创建 FlinkPipelineComposer
            Class<FlinkPipelineComposer> clazz =
                    (Class<FlinkPipelineComposer>)
                            Class.forName("org.apache.flink.cdc.composer.flink.FlinkPipelineComposer");
            java.lang.reflect.Constructor<FlinkPipelineComposer> constructor =
                    clazz.getDeclaredConstructor(org.apache.flink.streaming.api.environment.StreamExecutionEnvironment.class, boolean.class);
            constructor.setAccessible(true);

            // 创建 StreamExecutionEnvironment
            org.apache.flink.streaming.api.environment.StreamExecutionEnvironment env =
                    org.apache.flink.streaming.api.environment.StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(flinkConfig);

            FlinkPipelineComposer composer = constructor.newInstance(env, false);

            // 解析 pipeline 定义
            PipelineDef pipelineDef =
                    new YamlPipelineDefinitionParser()
                            .parse(param.getContent(), new Configuration());
            PipelineExecution.ExecutionInfo executionInfo = composer.compose(pipelineDef).execute();

            String jobName = pipelineDef.getConfig().get(PipelineOptions.PIPELINE_NAME);

            respDto
                    .setJobId(executionInfo.getId())
                    .setMessage("部署成功")
                    .setDeployMode("local")
                    .setWebInterfaceUrl(
                            "http://"
                                    + flinkConfig.get(RestOptions.ADDRESS)
                                    + ":"
                                    + (flinkConfig.get(RestOptions.PORT) == null
                                            ? "8081"
                                            : String.valueOf(flinkConfig.get(RestOptions.PORT))))
                    .setSubmitStatus(true)
                    .setConfig(flinkConfig.toMap())
                    .setSubmitTime(LocalDateTimeUtil.now());

        } catch (Exception e) {
            throw ServiceExceptionUtil.exception(new ErrorCode(9999, "本地数据摄取部署失败"), e);
        }
        return respDto;
    }

    @Override
    public JobDeployRespDto deployRemote(DeployRemoteDataIngestionParam param) {
        log.info("开始部署远程数据摄取任务");
        JobDeployRespDto respDto = new JobDeployRespDto();

        org.apache.flink.configuration.Configuration configuration = param.getConfiguration();
        FlinkPipelineComposer composer =
                FlinkPipelineComposer.ofRemoteCluster(configuration, java.util.Collections.emptyList());

        try {
            PipelineDef pipelineDef =
                    new YamlPipelineDefinitionParser()
                            .parse(param.getContent(), new Configuration());
            PipelineExecution.ExecutionInfo executionInfo = composer.compose(pipelineDef).execute();

            respDto
                    .setSubmitTime(LocalDateTimeUtil.now())
                    .setSubmitStatus(true)
                    .setJobId(executionInfo.getId())
                    .setDeployMode("remote")
                    .setMessage("部署成功")
                    .setConfig(configuration.toMap())
                    .setWebInterfaceUrl(
                            "http://"
                                    + configuration.getString(RestOptions.ADDRESS)
                                    + ":"
                                    + configuration.getInteger(RestOptions.PORT));
        } catch (Exception e) {
            throw ServiceExceptionUtil.exception(new ErrorCode(9999, "远程数据摄取部署失败"), e);
        }
        return respDto;
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
            ClusterClientProvider<ApplicationId> flinkApplicationCluster =
                    clusterDescriptor.deployInternal(
                            clusterSpecification,
                            "Flink application cluster",
                            YarnApplicationClusterEntryPoint.class.getName(),
                            null,
                            true);

            ClusterClient<ApplicationId> clusterClient = flinkApplicationCluster.getClusterClient();
            Map<String, String> config = clusterClient.getFlinkConfiguration().toMap();

            ApplicationReport applicationReport =
                    clusterDescriptor.getYarnClient().getApplicationReport(clusterClient.getClusterId());
            String trackingUrl = applicationReport.getTrackingUrl();

            // 等待作业启动并获取 JobDetails
            JobDetails jobDetails =
                    getJobDetails(trackingUrl, applicationReport, clusterDescriptor.getYarnClient(), 0);

            config.put("webUiUrl", trackingUrl);

            JobDeployRespDto respDto =
                    new JobDeployRespDto()
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
            log.error("YARN 数据摄取部署失败", e);
            throw ServiceExceptionUtil.exception(new ErrorCode(9999, "YARN 数据摄取部署失败"), e);
        }
    }

    /**
     * 获取 JobDetails，递归重试机制
     */
    private JobDetails getJobDetails(
            String trackingUrl,
            ApplicationReport applicationReport,
            YarnClient yarnClient,
            int retryCount) {
        if (retryCount > 5) {
            throw ServiceExceptionUtil.exception(new ErrorCode(9999, "获取作业详情失败，已达到最大重试次数"));
        }
        retryCount++;
        String content = HttpUtil.get(trackingUrl + JobsOverviewHeaders.URL);
        if (content == null) {
            throw ServiceExceptionUtil.exception(new ErrorCode(9999, "获取作业详情失败，响应为空"));
        }
        try {
            MultipleJobsDetails multipleJobsDetails =
                    RestMapperUtils.getStrictObjectMapper().readValue(content, MultipleJobsDetails.class);
            if (multipleJobsDetails.getJobs().isEmpty()) {
                applicationReport = yarnClient.getApplicationReport(applicationReport.getApplicationId());
                if (applicationReport.getYarnApplicationState().equals(YarnApplicationState.RUNNING)) {
                    Thread.sleep(1000L);
                    // 递归调用并返回结果
                    return getJobDetails(trackingUrl, applicationReport, yarnClient, retryCount);
                }
                // 如果不是RUNNING状态，抛出异常
                throw ServiceExceptionUtil.exception(new ErrorCode(9999, "获取作业详情失败，作业未在运行"));
            }

            JobDetails jobDetails = CollectionUtil.get(multipleJobsDetails.getJobs(), 0);
            return jobDetails;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw ServiceExceptionUtil.exception(new ErrorCode(9999, "获取作业详情失败"), e);
        }
    }

    /**
     * 获取 YarnClusterDescriptor
     */
    private YarnClusterDescriptor getClusterDescriptor(
            org.apache.flink.configuration.Configuration configuration,
            @NonNull String yarnSitePath,
            @NonNull String hdfsSitePath,
            @NonNull String coreSitePath) {
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

    /**
     * 获取 Yarn 配置
     */
    private static YarnConfiguration getYarnConfiguration(org.apache.flink.configuration.Configuration configuration) {
        YarnConfiguration yarnConfiguration = new YarnConfiguration();
        // 从 Flink 配置中获取 Hadoop 配置路径
        String hadoopConfDir = configuration.getString("pipeline.hadoop-conf-dir", null);
        if (hadoopConfDir != null) {
            yarnConfiguration.addResource(new File(hadoopConfDir, "yarn-site.xml").toURI().toString());
            yarnConfiguration.addResource(new File(hadoopConfDir, "core-site.xml").toURI().toString());
            yarnConfiguration.addResource(new File(hadoopConfDir, "hdfs-site.xml").toURI().toString());
        }
        return yarnConfiguration;
    }

}
