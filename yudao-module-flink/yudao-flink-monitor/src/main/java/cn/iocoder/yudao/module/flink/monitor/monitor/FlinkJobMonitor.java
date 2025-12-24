package cn.iocoder.yudao.module.flink.monitor.monitor;

import cn.iocoder.yudao.module.datastudio.api.enums.JobStatus;
import cn.iocoder.yudao.module.datastudio.api.job.dto.DataJobDto;
import org.springframework.lang.NonNull;

/**
 * Flink 作业监控接口
 *
 * <p>支持多种监控策略实现： - RestMonitor: 通过 REST API 获取作业状态 - ClusterClientMonitor: 通过 ClusterClient 获取作业状态
 *
 * @author yzh
 */
public interface FlinkJobMonitor {

  /**
   * 获取作业状态
   *
   * @param job 作业信息
   * @return 作业状态
   */
  @NonNull
  JobStatus getJobStatus(DataJobDto job) throws Exception;
}
