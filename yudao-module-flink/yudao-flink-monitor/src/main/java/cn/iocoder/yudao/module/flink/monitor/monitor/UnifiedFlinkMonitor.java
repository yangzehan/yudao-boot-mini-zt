package cn.iocoder.yudao.module.flink.monitor.monitor;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.thread.ExecutorBuilder;
import cn.hutool.core.thread.ThreadUtil;
import cn.iocoder.yudao.module.datastudio.api.enums.JobStatus;
import cn.iocoder.yudao.module.datastudio.api.job.DataJobApi;
import cn.iocoder.yudao.module.datastudio.api.job.dto.DataJobDto;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.PreDestroy;
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
 * @author yudao
 */
@Slf4j
@Component
public class UnifiedFlinkMonitor {
  private final DataJobApi dataJobApi;
  private final CompositeFlinkMonitor compositeFlinkMonitor;
  private final ExecutorService executorService;

  @Value("${flink.version}")
  private String flinkVersion;

  public UnifiedFlinkMonitor(DataJobApi dataJobApi, CompositeFlinkMonitor compositeFlinkMonitor) {
    this.dataJobApi = dataJobApi;
    this.compositeFlinkMonitor = compositeFlinkMonitor;
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
   * 监控单个作业并返回状态
   *
   * @param job 作业信息
   * @return 作业状态
   */
  private JobStatus monitorJob(DataJobDto job) {
    JobStatus status = compositeFlinkMonitor.getJobStatus(job);
    log.debug("作业 {} 状态: {}", job.getJobId(), status);
    return status;
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
