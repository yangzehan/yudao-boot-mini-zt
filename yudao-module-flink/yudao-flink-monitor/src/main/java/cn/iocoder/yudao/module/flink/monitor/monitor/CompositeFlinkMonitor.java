package cn.iocoder.yudao.module.flink.monitor.monitor;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.module.datastudio.api.enums.JobStatus;
import cn.iocoder.yudao.module.datastudio.api.job.dto.DataJobDto;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

/**
 * 组合监控器 实现监控策略链式调用和失败降级
 *
 * <p>监控策略：按顺序尝试每个监控器，每个监控器重试2次，间隔1秒
 *
 * @author yzh
 */
@Slf4j
@Configuration
public class CompositeFlinkMonitor implements FlinkJobMonitor {

  private final List<FlinkJobMonitor> monitors;
  private final RetryTemplate retryTemplate;

  public CompositeFlinkMonitor(
      @Lazy @Autowired(required = false) List<FlinkJobMonitor> monitors,
      @Autowired @Lazy RetryTemplate retryTemplate) {
    this.monitors = monitors != null ? monitors : new ArrayList<>();
    this.retryTemplate = retryTemplate;
  }

  @Bean
  public RetryTemplate retryTemplate() {
    RetryTemplate retryTemplate = new RetryTemplate();
    // 设置重试次数为 3（首次调用 + 2次重试）
    retryTemplate.setRetryPolicy(new SimpleRetryPolicy(3));
    // 设置固定等待时间 1 秒
    retryTemplate.setBackOffPolicy(new FixedBackOffPolicy());
    return retryTemplate;
  }

  @Override
  public @NonNull JobStatus getJobStatus(DataJobDto job) {
    for (FlinkJobMonitor monitor : monitors) {
      try {
        JobStatus status = executeWithRetry(monitor, job);
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

  /**
   * 使用 RetryTemplate 执行重试逻辑
   *
   * @param monitor 监控器
   * @param job 作业
   * @return 作业状态
   */
  private JobStatus executeWithRetry(FlinkJobMonitor monitor, DataJobDto job) throws Exception {
    return retryTemplate.execute(
        context -> monitor.getJobStatus(job),
        throwable -> {
          // 重试耗尽后的回调，返回 null 触发外层降级逻辑
          throw ServiceExceptionUtil.exception(
              new ErrorCode(9999, "{}"), throwable.getLastThrowable());
        });
  }
}
