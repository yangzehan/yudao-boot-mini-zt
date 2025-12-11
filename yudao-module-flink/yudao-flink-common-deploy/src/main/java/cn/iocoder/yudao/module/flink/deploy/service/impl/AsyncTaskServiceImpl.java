package cn.iocoder.yudao.module.flink.deploy.service.impl;


import cn.iocoder.yudao.module.flink.deploy.service.AsyncTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.runtime.minicluster.MiniCluster;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class AsyncTaskServiceImpl implements AsyncTaskService {

    @Async
    @Override
    public void MonitorClusters(MiniCluster miniCluster) {
        while (true){
            if (!miniCluster.isRunning()){
                break;
            }
        }
      log.info("MiniCluster停止执行");
    }
}
