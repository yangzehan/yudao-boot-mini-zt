package cn.iocoder.yudao.module.flink.monitor.monitor;

import cn.iocoder.yudao.module.datastudio.api.enums.JobStatus;
import cn.iocoder.yudao.module.datastudio.api.job.dto.DataJobDto;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import org.apache.flink.api.common.JobID;
import org.apache.flink.runtime.rest.RestClient;
import org.apache.flink.runtime.rest.messages.EmptyRequestBody;
import org.apache.flink.runtime.rest.messages.JobMessageParameters;
import org.apache.flink.runtime.rest.messages.job.JobStatusInfoHeaders;
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

  public RestMonitor(RestClient restClient) {
    this.restClient = restClient;
  }

  @NonNull
  @Override
  public JobStatus getJobStatus(DataJobDto job)
      throws IOException, ExecutionException, InterruptedException {
    final JobStatusInfoHeaders jobStatusInfoHeaders = JobStatusInfoHeaders.getInstance();
    final JobMessageParameters params = new JobMessageParameters();
    params.jobPathParameter.resolve(JobID.fromHexString(job.getJobId()));

    URL url = new URL(job.getWebUiUrl());
    org.apache.flink.api.common.JobStatus jobStatus =
        restClient
            .sendRequest(
                url.getHost(),
                url.getPort(),
                jobStatusInfoHeaders,
                params,
                EmptyRequestBody.getInstance())
            .get()
            .getJobStatus();
    return JobStatus.valueOf(jobStatus.name());
  }
}
