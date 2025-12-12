package cn.iocoder.yudao.module.flink.deploy.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.module.datastudio.api.enums.JobStatus;
import cn.iocoder.yudao.module.datastudio.api.job.DataJobApi;
import cn.iocoder.yudao.module.datastudio.api.job.dto.DataJobUpdateDto;
import cn.iocoder.yudao.module.flink.deploy.dto.ClusterMonitorDto;
import cn.iocoder.yudao.module.flink.deploy.service.ClusterMonitorService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.client.program.ClusterClient;
import org.apache.flink.runtime.client.JobStatusMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class ClusterMonitorServiceImpl implements ClusterMonitorService {
  private final Map<String, ClusterMonitorDto> clusterClientMap = new HashMap<>();
  @Resource private DataJobApi dataJobApi;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void register(String webInterfaceURL, ClusterClient<?> clusterClient, String jobId) {

    // 如果已经有了集群客户端  则查询一下是否有这个作业
    ClusterMonitorDto monitorDto = clusterClientMap.get(webInterfaceURL);
    if (ObjectUtil.isNull(monitorDto)) {
      log.info("注册新集群集群 webInterfaceURL{}", webInterfaceURL);
      ArrayList<String> jobIds = new ArrayList<>();
      jobIds.add(jobId);
      clusterClientMap.put(webInterfaceURL, new ClusterMonitorDto(clusterClient, jobIds));
    } else {
      // 查看集群是否存活，并且作业列表中是否存在原先作业列表的作业id，如果有则无需更改集群客户端，顺便更新一次集群作业列表，如果没有则说明集群变更，则需要更新集群客户端
      try {
        ClusterClient<?> trueClusterClient = clusterClientMap.get(webInterfaceURL).getClusterClient();
        // 无论如何都要更新集群作业列表
        Collection<JobStatusMessage> jobStatusMessages = trueClusterClient.listJobs().get();
        ArrayList<DataJobUpdateDto> updateDtos = new ArrayList<>();

        for (JobStatusMessage jobStatusMessage : jobStatusMessages) {
          // 只需要检查一个作业就可以判断是否为原集群,如果不存在则说明集群变更，需要将原先该webInterfaceURL的作业状态改为集群关闭不可知
          String jobIdTmp = jobStatusMessage.getJobId().toString();
          if (!jobId.equals(jobIdTmp) && monitorDto.getJobIds().contains(jobIdTmp)) {
            // 将原先集群中状态不明确的作业改为集群关闭不可知
            dataJobApi.updateJob(webInterfaceURL);
          }

          switch (jobStatusMessage.getJobState()) {
            case FAILED:
              updateDtos.add(new DataJobUpdateDto(jobIdTmp, JobStatus.FAILED));
              break;
            case CANCELED:
              updateDtos.add(new DataJobUpdateDto(jobIdTmp, JobStatus.CANCELED));
              break;
            case FINISHED:
              updateDtos.add(new DataJobUpdateDto(jobIdTmp, JobStatus.FINISHED));
              break;
            case RUNNING:
              updateDtos.add(new DataJobUpdateDto(jobIdTmp, JobStatus.RUNNING));
              break;
            case CREATED:
              updateDtos.add(new DataJobUpdateDto(jobIdTmp, JobStatus.CREATED));
              break;
            case SUSPENDED:
              updateDtos.add(new DataJobUpdateDto(jobIdTmp, JobStatus.SUSPENDED));
              break;
            case FAILING:
              updateDtos.add(new DataJobUpdateDto(jobIdTmp, JobStatus.FAILING));
              break;
            case CANCELLING:
              updateDtos.add(new DataJobUpdateDto(jobIdTmp, JobStatus.CANCELLING));
              break;
            case RESTARTING:
              updateDtos.add(new DataJobUpdateDto(jobIdTmp, JobStatus.RESTARTING));
              break;
            case RECONCILING:
              updateDtos.add(new DataJobUpdateDto(jobIdTmp, JobStatus.RECONCILING));
              break;
            case INITIALIZING:
              updateDtos.add(new DataJobUpdateDto(jobIdTmp, JobStatus.INITIALIZING));
              break;
          }
        }
        // 循环结束后统一更新所有作业状态
        dataJobApi.updateJobBatch(updateDtos);
      } catch (Exception e) {
        log.error("集群变更,更新作业状态失败", e);
        throw new RuntimeException(e);
      }
    }
  }
}
