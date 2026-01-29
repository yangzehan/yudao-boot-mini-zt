package cn.iocoder.yudao.module.flink.deploy.service;

import org.apache.flink.core.execution.JobClient;
import org.apache.flink.runtime.minicluster.MiniCluster;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * 异步任务服务 - 负责监控Flink集群状态
 *
 * <p>用于在Flink作业运行时监控集群状态，当所有作业都进入终态时自动关闭MiniCluster 此服务可在有Spring环境和无Spring环境下使用
 *
 * @author yzh
 */
public interface AsyncTaskService {

  /**
   * 监控Flink集群状态
   *
   * @param miniCluster 要监控的MiniCluster实例
   * @param async
   */
  void monitorClusters(MiniCluster miniCluster, boolean async);

  void monitorClustersAsync(MiniCluster miniCluster);

  void monitorEnvByJobClient(JobClient jobClient, StreamExecutionEnvironment env) throws Exception;
}
