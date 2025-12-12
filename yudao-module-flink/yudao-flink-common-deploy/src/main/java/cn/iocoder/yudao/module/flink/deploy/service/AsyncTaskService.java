package cn.iocoder.yudao.module.flink.deploy.service;

import org.apache.flink.runtime.minicluster.MiniCluster;

public interface AsyncTaskService {


    void monitorClusters(MiniCluster miniCluster);


}
