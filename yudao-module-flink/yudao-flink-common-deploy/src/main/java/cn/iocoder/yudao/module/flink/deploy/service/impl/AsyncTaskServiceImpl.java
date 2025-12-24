package cn.iocoder.yudao.module.flink.deploy.service.impl;

import cn.iocoder.yudao.module.flink.deploy.service.AsyncTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.runtime.client.JobStatusMessage;
import org.apache.flink.runtime.minicluster.MiniCluster;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AsyncTaskServiceImpl implements AsyncTaskService {

  @Async
  @Override
  public void monitorClusters(MiniCluster miniCluster) {

    while (!miniCluster.isRunning()) {
      try {
        for (JobStatusMessage jobStatusMessage : miniCluster.listJobs().get()) {
          boolean isDone = jobStatusMessage.getJobState().isGloballyTerminalState();
          if (isDone) {
            miniCluster.close();
            break;
          }
        }
      } catch (Exception e) {
        log.error("本地集群已经关闭或者无法访问", e);
      }
    }
  }
}
