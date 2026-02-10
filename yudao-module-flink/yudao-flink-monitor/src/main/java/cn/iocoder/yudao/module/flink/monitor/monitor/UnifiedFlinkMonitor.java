package cn.iocoder.yudao.module.flink.monitor.monitor;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.thread.ExecutorBuilder;
import cn.hutool.core.thread.ThreadUtil;
import cn.iocoder.yudao.module.datastudio.api.enums.JobStatus;
import cn.iocoder.yudao.module.datastudio.api.job.DataJobApi;
import cn.iocoder.yudao.module.datastudio.api.job.dto.DataJobDto;
import cn.iocoder.yudao.module.flink.monitor.alert.event.AlertEventPublisher;
import cn.iocoder.yudao.module.flink.monitor.alert.model.AlertLevel;
import cn.iocoder.yudao.module.flink.monitor.alert.model.AlertMessage;
import cn.iocoder.yudao.module.flink.monitor.alert.model.AlertType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 统一 Flink 监控服务
 *
 * <p>整合三层监控策略，提供统一的监控接口
 *
 * <p>支持的部署模式: - Local Standalone + ZK HA - Remote Standalone + ZK HA - Yarn Session - Yarn
 * Application - Kubernetes Session - Kubernetes Application
 *
 * <p>扩展: 集成告警功能 - 作业失败时触发邮件告警
 *
 * @author yudao
 */
@Slf4j
@Component
public class UnifiedFlinkMonitor {

  private final DataJobApi dataJobApi;
  private final CompositeFlinkMonitor compositeFlinkMonitor;
  private final AlertEventPublisher alertEventPublisher;
  private final ExecutorService executorService;

  @Value("${flink.version}")
  private String flinkVersion;

  /**
   * 用于存储作业上一次状态 (jobId -> oldStatus)
   */
  private final Map<String, JobStatus> previousStatusMap = new java.util.concurrent.ConcurrentHashMap<>();

  public UnifiedFlinkMonitor(
          DataJobApi dataJobApi,
          CompositeFlinkMonitor compositeFlinkMonitor,
          AlertEventPublisher alertEventPublisher) {
    this.dataJobApi = dataJobApi;
    this.compositeFlinkMonitor = compositeFlinkMonitor;
    this.alertEventPublisher = alertEventPublisher;
    this.executorService =
        ExecutorBuilder.create()
            .setCorePoolSize(2)
            .setMaxPoolSize(10)
            .setKeepAliveTime(10L, TimeUnit.MINUTES)
            .setThreadFactory(
                ThreadUtil.createThreadFactoryBuilder().setNamePrefix("monitor-").build())
            .build();
  }

  /**
   * 监控单个作业并返回状态，同时检测状态变化
   *
   * @param job 作业信息
   * @return 作业状态
   */
  private JobStatus monitorJob(DataJobDto job) {
    // 获取当前状态
    JobStatus newStatus = compositeFlinkMonitor.getJobStatus(job);

    // 获取之前状态
    JobStatus oldStatus = previousStatusMap.get(job.getJobId());

    // 检测到状态变化，触发告警检查
    if (oldStatus != null && !newStatus.equals(oldStatus)) {
      log.info("作业状态变化: jobId={}, {} -> {}",
              job.getJobId(), oldStatus, newStatus);
      handleStatusChange(job, oldStatus, newStatus);
    }

    // 更新状态缓存
    previousStatusMap.put(job.getJobId(), newStatus);

    log.debug("作业 {} 状态: {}", job.getJobId(), newStatus);
    return newStatus;
  }

  /**
   * 处理状态变化
   *
   * @param job       作业信息
   * @param oldStatus 旧状态
   * @param newStatus 新状态
   */
  private void handleStatusChange(DataJobDto job, JobStatus oldStatus, JobStatus newStatus) {
    // 仅在作业失败时触发告警
    if (newStatus == JobStatus.FAILED) {
      triggerJobFailedAlert(job, newStatus);
    }

    // 如果作业从失败状态恢复，可以发送恢复通知（可选）
    if (oldStatus == JobStatus.FAILED && newStatus == JobStatus.RUNNING) {
      triggerJobRecoveryAlert(job);
    }

    // TODO: 其他状态变化处理...
    // 例如：CANCELED, RESTARTING 等
  }

  /**
   * 触发作业失败告警
   */
  private void triggerJobFailedAlert(DataJobDto job, JobStatus status) {
    try {
      // 获取错误信息 (如果有)
      String errorMsg = getErrorMessage(job);

      // 提取作业名称
      String jobName = extractJobName(job);

      // 获取集群ID
      Long clusterId = getClusterId(job);

      // 创建告警消息
      AlertMessage alertMessage = AlertMessage.builder()
              .jobId(job.getJobId())
              .jobName(jobName)
              .clusterId(clusterId)
              .type(AlertType.JOB_FAILED)
              .level(AlertLevel.CRITICAL)
              .errorMsg(errorMsg)
              .occurredTime(LocalDateTime.now())
              .build();

      // 发布告警事件
      alertEventPublisher.publish(alertMessage);

      log.info("作业失败告警已触发: jobId={}, jobName={}",
              job.getJobId(), jobName);

    } catch (Exception e) {
      log.error("触发作业失败告警失败: jobId={}", job.getJobId(), e);
    }
  }

  /**
   * 触发作业恢复通知
   */
  private void triggerJobRecoveryAlert(DataJobDto job) {
    try {
      String jobName = extractJobName(job);
      Long clusterId = getClusterId(job);

      AlertMessage alertMessage = AlertMessage.builder()
              .jobId(job.getJobId())
              .jobName(jobName)
              .clusterId(clusterId)
              .type(AlertType.JOB_RESTART)
              .level(AlertLevel.INFO)
              .occurredTime(LocalDateTime.now())
              .recovered(true)
              .build();

      alertEventPublisher.publishRecovery(alertMessage);

      log.info("作业恢复通知已触发: jobId={}, jobName={}",
              job.getJobId(), jobName);

    } catch (Exception e) {
      log.error("触发作业恢复通知失败: jobId={}", job.getJobId(), e);
    }
  }

  /**
   * 从作业配置中提取作业名称
   */
  private String extractJobName(DataJobDto job) {
    if (job.getConfig() != null) {
      String name = job.getConfig().get("job.name");
      if (name != null && !name.isEmpty()) {
        return name;
      }
    }
    // 如果配置中没有，尝试从其他字段获取
    if (job.getJobId() != null) {
      return "Unknown-" + job.getJobId();
    }
    return "Unknown-Job";
  }

  /**
   * 获取集群ID
   */
  private Long getClusterId(DataJobDto job) {
    if (job.getClusterId() != null) {
      return job.getClusterId();
    }
    if (job.getFlinkClusterId() != null) {
      try {
        return Long.parseLong(job.getFlinkClusterId());
      } catch (NumberFormatException e) {
        log.debug("无法解析 flinkClusterId: {}", job.getFlinkClusterId());
      }
    }
    return null;
  }

  /**
   * 获取错误信息
   */
  private String getErrorMessage(DataJobDto job) {
    // TODO: 从监控结果中获取错误信息
    // 可以通过查询 REST API 获取异常信息
    // 目前返回通用信息
    return "作业状态变为 FAILED，请检查 Flink Web UI 获取详细信息";
  }

  /** 定时监控所有运行中的作业 */
  @Scheduled(fixedDelay = 5L, timeUnit = TimeUnit.SECONDS)
  @Async
  public void monitor() {
    log.debug("开始执行作业监控任务");

    // 查询需要监控的作业列表
    List<DataJobDto> runningJobs =
        dataJobApi
            .listJob(
                CollectionUtil.toList(JobStatus.RUNNING, JobStatus.CREATED, JobStatus.INITIALIZING),
                flinkVersion)
            .getCheckedData();
    if (CollectionUtil.isEmpty(runningJobs)) {
      log.debug("没有需要监控的运行中作业");
      return;
    }
    log.debug("开始监控 {} 个运行中的作业", runningJobs.size());

    for (DataJobDto runningJob : runningJobs) {
      if (!dataJobApi.shouldMonitor(runningJob.getJobId()).getCheckedData()) {
        log.warn("作业正在检查中无需再次监控");
        continue;
      }
      try {
        ThreadUtil.newCompletionService(executorService)
            .submit(
                new Callable<Object>() {
                  @Override
                  public JobStatus call() throws Exception {
                    JobStatus jobStatus = monitorJob(runningJob);
                    dataJobApi.monitorJobFinished(runningJob.getJobId(), jobStatus);
                    return jobStatus;
                  }
                })
            .get();
      } catch (InterruptedException | ExecutionException e) {
        throw new RuntimeException(e);
      }
    }

    log.debug("作业监控任务完成");
  }

  @PreDestroy
  public void destroy() {
    executorService.shutdown();
  }
}
