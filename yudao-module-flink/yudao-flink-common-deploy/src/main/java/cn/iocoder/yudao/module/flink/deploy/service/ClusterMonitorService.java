package cn.iocoder.yudao.module.flink.deploy.service;

import org.apache.flink.client.program.ClusterClient;

public interface ClusterMonitorService {
  void register(String webInterfaceURL, ClusterClient<?> clusterClient, String jobId);
}
