package cn.iocoder.yudao.module.flink.deploy.service;

import org.apache.flink.runtime.minicluster.MiniCluster;
import org.springframework.scheduling.annotation.Async;

public interface AsyncTaskService {


    void MonitorClusters(MiniCluster miniCluster);


}
