package cn.iocoder.yudao.module.flink.monitor.monitor;

import cn.iocoder.yudao.module.datastudio.api.enums.JobStatus;
import cn.iocoder.yudao.module.datastudio.api.job.dto.DataJobDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * YARN Application 模式专用监控器
 *
 * <p>当 REST API 和 ClusterClient 都不可用时，通过 YARN ResourceManager API 查询 Application 状态
 *
 * @author yzh
 */
@Slf4j
@Component
@Order(3)  // 优先级最低，作为最后降级方案
public class YarnClientMonitor implements FlinkJobMonitor {

  @Override
  public @NonNull JobStatus getJobStatus(@Nullable DataJobDto job) throws Exception {
    if (job == null) {
      throw new IllegalArgumentException("job 不能为空");
    }

    // 1. 检查是否为 YARN Application 模式
    if (!isYarnApplicationMode(job)) {
      throw new IllegalStateException("非 YARN Application 模式，跳过 YARN 监控");
    }

    // 2. 检查 flinkClusterId 是否存在
    String flinkClusterId = job.getFlinkClusterId();
    if (flinkClusterId == null || flinkClusterId.isEmpty()) {
      throw new IllegalStateException("flinkClusterId 为空，无法查询 YARN 应用状态");
    }

    // 3. 检查 YARN 配置路径是否齐全
    String yarnSitePath = job.getYarnSitePath();
    String hdfsSitePath = job.getHdfsSitePath();
    String coreSitePath = job.getCoreSitePath();

    if (yarnSitePath == null || yarnSitePath.isEmpty()
        || hdfsSitePath == null || hdfsSitePath.isEmpty()
        || coreSitePath == null || coreSitePath.isEmpty()) {
      throw new IllegalStateException("YARN 配置路径不齐全，无法创建 YarnClient");
    }

    log.debug("使用 YarnClient 监控作业: jobId={}, appId={}",
        job.getJobId(), flinkClusterId);

    // 4. 创建 YarnConfiguration
    YarnConfiguration yarnConfig = new YarnConfiguration();
    yarnConfig.addResource(new Path(hdfsSitePath));
    yarnConfig.addResource(new Path(coreSitePath));
    yarnConfig.addResource(new Path(yarnSitePath));

    // 5. 创建并启动 YarnClient
    YarnClient yarnClient = YarnClient.createYarnClient();
    yarnClient.init(yarnConfig);
    yarnClient.start();

    try {
      // 6. 解析 ApplicationId
      ApplicationId appId = ApplicationId.fromString(flinkClusterId);

      // 7. 查询 Application 状态
      ApplicationReport report = yarnClient.getApplicationReport(appId);
      YarnApplicationState yarnState = report.getYarnApplicationState();

      // 8. 映射到系统状态
      JobStatus status = mapYarnStateToJobStatus(yarnState);

      log.debug("YARN 监控结果: jobId={}, appId={}, yarnState={}, systemStatus={}",
          job.getJobId(), flinkClusterId, yarnState, status);

      return status;

    } finally {
      yarnClient.stop();
    }
  }

  /**
   * 判断是否为 YARN Application 模式
   */
  protected boolean isYarnApplicationMode(DataJobDto job) {
    // 1. 优先从 config 中获取 deployMode
    java.util.Map<String, String> config = job.getConfig();
    if (config != null) {
      String deployMode = config.get("deployMode");
      if ("application".equalsIgnoreCase(deployMode)) {
        return true;
      }
    }

    // 2. 从 flinkClusterId 格式判断 (YARN ApplicationId 格式: application_xxx_xxx)
    String flinkClusterId = job.getFlinkClusterId();
    if (flinkClusterId != null && flinkClusterId.startsWith("application_")) {
      return true;
    }

    return false;
  }

  /**
   * YARN 状态 → 系统状态映射
   */
  protected JobStatus mapYarnStateToJobStatus(YarnApplicationState yarnState) {
    switch (yarnState) {
      case ACCEPTED:
        return JobStatus.INITIALIZING;   // 作业已接受，等待调度
      case SUBMITTED:
        return JobStatus.CREATED;        // 作业已提交
      case RUNNING:
        return JobStatus.RUNNING;        // 作业运行中
      case FINISHED:
        return JobStatus.FINISHED;       // 作业完成成功
      case FAILED:
        return JobStatus.FAILED;         // 作业失败
      case KILLED:
        return JobStatus.CANCELED;       // 作业被手动终止
      default:
        return JobStatus.UNKNOWN;        // 未知状态
    }
  }
}
