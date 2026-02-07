package cn.iocoder.yudao.module.flink.deploy.util.yarn;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import java.io.File;
import java.util.Map;
import org.apache.flink.client.deployment.ClusterSpecification;
import org.apache.flink.client.program.ClusterClient;
import org.apache.flink.client.program.ClusterClientProvider;
import org.apache.flink.runtime.messages.webmonitor.JobDetails;
import org.apache.flink.runtime.messages.webmonitor.MultipleJobsDetails;
import org.apache.flink.runtime.rest.messages.JobsOverviewHeaders;
import org.apache.flink.runtime.rest.util.RestMapperUtils;
import org.apache.flink.yarn.YarnClientYarnClusterInformationRetriever;
import org.apache.flink.yarn.entrypoint.YarnApplicationClusterEntryPoint;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.springframework.lang.NonNull;

/**
 * @author yzh
 */
public class ClusterDescriptorUtil {
  /** 获取 YarnClusterDescriptor */
  public static YarnClusterDescriptor getClusterDescriptor(
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

  public static Result deployYarnApplication(
      YarnClusterDescriptor clusterDescriptor, ClusterSpecification clusterSpecification)
      throws Exception {
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
    return new Result(clusterClient, config, trackingUrl, jobDetails);
  }

  /** 获取 JobDetails，递归重试机制 */
  private static JobDetails getJobDetails(
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

      return CollectionUtil.get(multipleJobsDetails.getJobs(), 0);
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw ServiceExceptionUtil.exception(new ErrorCode(9999, "获取作业详情失败"), e);
    }
  }

  /** 获取 Yarn 配置 */
  private static YarnConfiguration getYarnConfiguration(
      org.apache.flink.configuration.Configuration configuration) {
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

  public static class Result {
    public final ClusterClient<ApplicationId> clusterClient;
    public final Map<String, String> config;
    public final String trackingUrl;
    public final JobDetails jobDetails;

    public Result(
        ClusterClient<ApplicationId> clusterClient,
        Map<String, String> config,
        String trackingUrl,
        JobDetails jobDetails) {
      this.clusterClient = clusterClient;
      this.config = config;
      this.trackingUrl = trackingUrl;
      this.jobDetails = jobDetails;
    }
  }
}
