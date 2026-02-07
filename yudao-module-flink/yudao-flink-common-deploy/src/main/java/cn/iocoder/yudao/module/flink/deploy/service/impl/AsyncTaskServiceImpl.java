package cn.iocoder.yudao.module.flink.deploy.service.impl;

import cn.iocoder.yudao.module.flink.deploy.service.AsyncTaskService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.core.execution.JobClient;
import org.apache.flink.runtime.client.JobStatusMessage;
import org.apache.flink.runtime.minicluster.MiniCluster;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * 异步任务服务实现 - 负责监控Flink集群状态
 *
 * <p>用于在Flink作业运行时监控集群状态，当所有作业都进入终态时自动关闭MiniCluster 此实现不依赖Spring，可在有Spring环境和无Spring环境下使用
 *
 * @author yzh
 */
@Slf4j
public class AsyncTaskServiceImpl implements AsyncTaskService {

  private ExecutorService executorService;

  public AsyncTaskServiceImpl() {}

  @Override
  public void monitorClustersAsync(MiniCluster miniCluster) {
    createExecutorService();
    executorService.submit(
        () -> {
          monitorClustersSync(miniCluster);
        });
  }

  private void createExecutorService() {
    this.executorService =
        Executors.newCachedThreadPool(
            new ThreadFactory() {
              private final AtomicInteger counter = new AtomicInteger(0);

              @Override
              public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("flink-cluster-monitor-" + counter.getAndIncrement());
                thread.setDaemon(true);
                return thread;
              }
            });
  }

  @Override
  public void monitorEnvByJobClient(JobClient jobClient, StreamExecutionEnvironment env)
      throws Exception {
    createExecutorService();
    executorService.submit(
        () -> {
          try (StreamExecutionEnvironment ignored = env) {
            jobClient.getJobExecutionResult().get();
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        });
  }

  @Override
  public void monitorClusters(MiniCluster miniCluster, boolean async) {
    if (async) {
      monitorClustersAsync(miniCluster);
    } else {
      monitorClustersSync(miniCluster);
    }
  }

  public void monitorClustersSync(MiniCluster miniCluster) {
    while (miniCluster.isRunning()) {
      try {
        for (JobStatusMessage jobStatusMessage : miniCluster.listJobs().get()) {
          boolean isDone = jobStatusMessage.getJobState().isGloballyTerminalState();
          if (isDone) {
            miniCluster.close();
            log.info("作业 [{}] 已进入终态，关闭 MiniCluster", jobStatusMessage.getJobId());
            Thread.sleep(5000);
            break;
          }
        }
        // 短暂休眠避免过度占用CPU
        Thread.sleep(1000);
      } catch (Exception e) {
        log.error("本地集群已经关闭或者无法访问", e);
        try {
          miniCluster.close();
        } catch (Exception closeEx) {
          log.error("关闭 MiniCluster 失败", closeEx);
        }
        break;
      }
    }
  }
}
