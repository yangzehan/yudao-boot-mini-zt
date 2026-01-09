package cn.iocoder.yudao.module.flink.monitor.monitor;

import cn.iocoder.yudao.module.datastudio.api.enums.JobStatus;
import cn.iocoder.yudao.module.datastudio.api.job.dto.DataJobDto;
import java.net.URL;
import java.util.concurrent.Executor;
import org.apache.flink.api.common.JobID;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.runtime.rest.RestClient;
import org.apache.flink.runtime.rest.messages.EmptyRequestBody;
import org.apache.flink.runtime.rest.messages.JobMessageParameters;
import org.apache.flink.runtime.rest.messages.job.JobStatusInfoHeaders;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * REST API 监控器 通过 Flink REST API 获取作业状态
 *
 * @author yzh
 */
@Order(1)
@Component
public class RestMonitor implements FlinkJobMonitor {

  private final RestClient restClient;
  private final Configuration clientConfiguration;
  private final Executor monitorExecutor;

  public RestMonitor(
      RestClient restClient,
      org.apache.flink.configuration.Configuration clientConfiguration,
      @Qualifier(value = "monitorExecutor") Executor monitorExecutor) {
    this.restClient = restClient;
    this.clientConfiguration = clientConfiguration;
    this.monitorExecutor = monitorExecutor;
  }

  @NonNull
  @Override
  public JobStatus getJobStatus(DataJobDto job) throws Exception {

    RestClient restClient = this.restClient;
    org.apache.flink.api.common.JobStatus jobStatus;
    final JobStatusInfoHeaders jobStatusInfoHeaders = JobStatusInfoHeaders.getInstance();
    final JobMessageParameters params = new JobMessageParameters();
    params.jobPathParameter.resolve(JobID.fromHexString(job.getJobId()));
    String webUiUrl = job.getWebUiUrl();
    if (webUiUrl.endsWith("/")) {
      Configuration yarnRestConfiguration = new Configuration(clientConfiguration);
      yarnRestConfiguration.set(RestOptions.URL_PREFIX, "/proxy/" + job.getFlinkClusterId() + "/");
      try (RestClient yarnRestClient = new RestClient(yarnRestConfiguration, monitorExecutor)) {
        webUiUrl = webUiUrl.substring(0, webUiUrl.length() - 1);
        URL url = new URL(webUiUrl);
        jobStatus =
            yarnRestClient
                .sendRequest(
                    url.getHost(),
                    url.getPort(),
                    jobStatusInfoHeaders,
                    params,
                    EmptyRequestBody.getInstance())
                .get()
                .getJobStatus();
      }

    } else {
      URL url = new URL(webUiUrl);
      jobStatus =
          restClient
              .sendRequest(
                  url.getHost(),
                  url.getPort(),
                  jobStatusInfoHeaders,
                  params,
                  EmptyRequestBody.getInstance())
              .get()
              .getJobStatus();
    }

    return JobStatus.valueOf(jobStatus.name());
  }
}
