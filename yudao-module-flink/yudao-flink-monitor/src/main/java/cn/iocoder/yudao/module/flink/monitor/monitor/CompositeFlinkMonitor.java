package cn.iocoder.yudao.module.flink.monitor.monitor;

import cn.iocoder.yudao.module.datastudio.api.enums.JobStatus;
import cn.iocoder.yudao.module.datastudio.api.job.dto.DataJobDto;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * 组合监控器 实现监控策略链式调用和失败降级
 *
 * <p>监控策略：按顺序尝试每个监控器，失败则降级到下一个
 *
 * @author yzh
 */
@Slf4j
@Component
public class CompositeFlinkMonitor implements FlinkJobMonitor {

  private final List<FlinkJobMonitor> monitors;

  public CompositeFlinkMonitor(@Lazy @Autowired(required = false) List<FlinkJobMonitor> monitors) {
    this.monitors = monitors != null ? monitors : new ArrayList<>();
  }

  @Override
  public @NonNull JobStatus getJobStatus(DataJobDto job) {
    for (FlinkJobMonitor monitor : monitors) {
      try {
        JobStatus status = monitor.getJobStatus(job);
        log.debug(
            "使用 {} 监控器获取作业 {} 状态成功: {}",
            monitor.getClass().getSimpleName(),
            job.getJobId(),
            status);
        return status;
      } catch (Exception e) {
        log.warn(
            "使用 {} 监控器获取作业 {} 状态异常，降级到下一个监控器",
            monitor.getClass().getSimpleName(),
            job.getJobId(),
            e);
      }
    }
    log.error("所有监控器都无法获取作业 {} 的状态", job.getJobId());
    return JobStatus.UNKNOWN;
  }
}
