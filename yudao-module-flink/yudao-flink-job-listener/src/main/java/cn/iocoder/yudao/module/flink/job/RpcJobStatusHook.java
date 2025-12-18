package cn.iocoder.yudao.module.flink.job;

import cn.hutool.http.HttpUtil;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.client.naming.NacosNamingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.flink.api.common.JobID;
import org.apache.flink.core.execution.JobStatusHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Flink 作业状态监控 Hook 在作业状态变化时调用 DataJob API 更新作业状态
 *
 * @author yzh
 */
public class RpcJobStatusHook implements JobStatusHook {

  private static final Logger log = LoggerFactory.getLogger(RpcJobStatusHook.class);

  private static final long serialVersionUID = -6298413079218422319L;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final String serverAddr;
  private final String namespace;
  private NacosNamingService nacosNamingService;

  public RpcJobStatusHook(String serverAddr, String namespace) {
    this.serverAddr = serverAddr;
    this.namespace = namespace;
  }

  @Override
  public void onCreated(JobID jobId) {
    log.info("作业已创建，JobID: {}", jobId);
    updateJobStatus(jobId, "CREATED");
  }

  @Override
  public void onFinished(JobID jobId) {
    log.info("作业已完成，JobID: {}", jobId);
    updateJobStatus(jobId, "FINISHED");
  }

  @Override
  public void onFailed(JobID jobId, Throwable throwable) {
    log.error("作业执行失败，JobID: {}, 错误信息: {}", jobId, throwable.getMessage(), throwable);
    updateJobStatus(jobId, "FAILED");
  }

  @Override
  public void onCanceled(JobID jobId) {
    log.info("作业已取消，JobID: {}", jobId);
    updateJobStatus(jobId, "CANCELED");
  }

  private void init() {
    if (this.nacosNamingService == null) {
      Properties properties = new Properties();
      properties.put("serverAddr", serverAddr);
      properties.put("namespace", namespace);
      try {
        nacosNamingService = new NacosNamingService(properties);
      } catch (NacosException e) {
        log.error("初始化 Nacos 失败", e);
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * 更新作业状态
   *
   * @param jobId Flink 作业ID
   * @param status Flink 作业状态
   */
  private void updateJobStatus(JobID jobId, String status) {
    try {
      init();
      // 通过nacosService 获取yudao-server的实例  然后完成调用/data-studio/job/update
      DataJobUpdateDto dto = new DataJobUpdateDto(jobId.toString(), status);
      List<Instance> instances = nacosNamingService.getAllInstances("yudao-server");
      Instance instance = instances.get(ThreadLocalRandom.current().nextInt(instances.size()));
      // 构建url
      String url = "http://" + instance.toInetAddr() + "/data-studio/job/update";
      String data = HttpUtil.post(url, objectMapper.writeValueAsString(dto));
      if (objectMapper.readTree(data).get("code").asInt() == 0) {
        log.info("更新作业状态成功，JobID: {}, status: {}", jobId, status);
      } else {
        log.error("更新作业状态失败，JobID: {}, status: {},respond{}", jobId, status, data);
      }
    } catch (Exception e) {
      log.error("更新作业状态失败，JobID: {}, status: {}", jobId, status, e);
    }
  }
}
