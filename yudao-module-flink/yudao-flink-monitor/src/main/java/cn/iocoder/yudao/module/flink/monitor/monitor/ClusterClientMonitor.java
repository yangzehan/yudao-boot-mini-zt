package cn.iocoder.yudao.module.flink.monitor.monitor;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.module.datastudio.api.enums.JobStatus;
import cn.iocoder.yudao.module.datastudio.api.job.dto.DataJobDto;
import java.util.concurrent.TimeUnit;
import org.apache.flink.api.common.JobID;
import org.apache.flink.client.deployment.StandaloneClusterId;
import org.apache.flink.client.program.rest.RestClusterClient;
import org.apache.flink.configuration.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * ClusterClient 监控器 通过 ClusterClient 获取作业状态
 *
 * @author yzh
 */
@Order(2)
@Component
public class ClusterClientMonitor implements FlinkJobMonitor {

  @NonNull
  @Override
  public JobStatus getJobStatus(DataJobDto job) throws Exception {
    RestClusterClient clusterClient;
    if (ObjUtil.isNull(job.getFlinkClusterId())) {
      clusterClient =
          new RestClusterClient<>(
              Configuration.fromMap(job.getConfig()), StandaloneClusterId.getInstance());
    } else {
      clusterClient =
          new RestClusterClient<>(Configuration.fromMap(job.getConfig()), job.getFlinkClusterId());
    }
    org.apache.flink.api.common.JobStatus jobStatus =
        (org.apache.flink.api.common.JobStatus)
            clusterClient
                .getJobStatus(JobID.fromHexString(job.getJobId()))
                .get(2L, TimeUnit.SECONDS);
    clusterClient.close();
    return JobStatus.valueOf(jobStatus.name());
  }
}
