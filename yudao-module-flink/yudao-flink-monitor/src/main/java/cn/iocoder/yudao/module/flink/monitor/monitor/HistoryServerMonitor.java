package cn.iocoder.yudao.module.flink.monitor.monitor;

import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.module.datastudio.api.enums.JobStatus;
import cn.iocoder.yudao.module.datastudio.api.job.dto.DataJobDto;
import cn.iocoder.yudao.module.flink.common.dto.HistoryJobInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * History Server 监控器
 *
 * <p>通过 Flink History Server REST API 获取历史作业信息。
 *
 * @author yzh
 */
@Slf4j
@Order(4)
@Component
public class HistoryServerMonitor implements FlinkJobMonitor {

  /** History Server /jobs/overview 端点路径 */
  private static final String JOBS_OVERVIEW_URL = "/jobs/overview";

  private final String historyServerUrl;

  public HistoryServerMonitor(@Value("${flink.history.server.url}") String historyServerUrl) {
    this.historyServerUrl = historyServerUrl;
  }

  /**
   * 检查 History Server 是否已配置
   *
   * @return true 表示已配置
   */
  public boolean isEnabled() {
    return historyServerUrl != null && !historyServerUrl.isEmpty();
  }

  @NonNull
  @Override
  public JobStatus getJobStatus(DataJobDto job) throws Exception {
    if (!isEnabled()) {
      log.debug("History Server 未配置，跳过监控");
      return JobStatus.UNKNOWN;
    }

    try {
      HistoryJobInfo historyJobInfo = getHistoryJobInfo(job.getJobId());
      if (historyJobInfo == null) {
        return JobStatus.UNKNOWN;
      }
      return parseJobStatus(historyJobInfo.getState());
    } catch (Exception e) {
      log.warn("获取历史作业 [{}] 状态失败: {}", job.getJobId(), e.getMessage());
      return JobStatus.UNKNOWN;
    }
  }

  /**
   * 获取历史作业信息
   *
   * @param jobId 作业ID
   * @return 历史作业信息，未找到返回 null
   */
  public HistoryJobInfo getHistoryJobInfo(String jobId) {
    if (!isEnabled()) {
      return null;
    }

    try {
      List<HistoryJobInfo> jobs = getCompletedJobs();
      return jobs.stream().filter(job -> job.getJobId().equals(jobId)).findFirst().orElse(null);
    } catch (Exception e) {
      log.warn("获取历史作业 [{}] 信息失败: {}", jobId, e.getMessage());
      return null;
    }
  }

  /**
   * 获取已完成的历史作业列表
   *
   * @return 历史作业列表
   */
  public List<HistoryJobInfo> getCompletedJobs() {
    if (!isEnabled()) {
      return Collections.emptyList();
    }

    try {
      URL url = parseUrl(historyServerUrl);
      String requestUrl = url + JOBS_OVERVIEW_URL;
      log.debug("获取历史作业列表 from {}", requestUrl);

      // 调用 History Server /jobs/overview 端点
      String response = HttpUtil.get(requestUrl);
      if (response == null || response.isEmpty()) {
        log.warn("获取历史作业列表响应为空");
        return Collections.emptyList();
      }

      // 解析 JSON 响应
      List<HistoryJobInfo> historyJobs = parseJobsOverviewResponse(response);
      log.debug("获取到 {} 个历史作业", historyJobs.size());
      return historyJobs;
    } catch (Exception e) {
      log.warn("获取历史作业列表失败: {}", e.getMessage());
      return Collections.emptyList();
    }
  }

  /**
   * 解析 /jobs/overview 端点返回的 JSON 响应
   *
   * @param response JSON 响应字符串
   * @return 历史作业信息列表
   */
  private List<HistoryJobInfo> parseJobsOverviewResponse(String response) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      JsonNode root = objectMapper.readTree(response);
      JsonNode jobsNode = root.get("jobs");

      if (jobsNode == null || !jobsNode.isArray()) {
        return Collections.emptyList();
      }

      return Lists.newArrayList(jobsNode.elements()).stream()
          .map(this::parseJobDetailsNode)
          .filter(job -> job != null)
          .collect(Collectors.toList());
    } catch (Exception e) {
      log.warn("解析历史作业列表失败: {}", e.getMessage());
      return Collections.emptyList();
    }
  }

  /**
   * 解析单个作业详情节点
   *
   * @param node JSON 节点
   * @return 历史作业信息
   */
  private HistoryJobInfo parseJobDetailsNode(JsonNode node) {
    try {
      return HistoryJobInfo.builder()
          .jobId(node.path("id").asText(null))
          .jobName(node.path("name").asText(null))
          .state(node.path("status").asText(null))
          .startTime(node.path("start-time").asLong(0L))
          .endTime(node.path("end-time").asLong(0L))
          .duration(node.path("duration").asLong(0L))
          .lastError(node.path("last-error").asText(null))
          .build();
    } catch (Exception e) {
      log.warn("解析作业详情失败: {}", e.getMessage());
      return null;
    }
  }

  /**
   * 获取单个历史作业详情
   *
   * @param jobId 作业ID
   * @return 历史作业详情
   */
  public Optional<HistoryJobInfo> getJobDetail(String jobId) {
    if (!isEnabled()) {
      return Optional.empty();
    }

    try {
      HistoryJobInfo historyJobInfo = getHistoryJobInfo(jobId);
      return Optional.ofNullable(historyJobInfo);
    } catch (Exception e) {
      log.warn("获取历史作业 [{}] 详情失败: {}", jobId, e.getMessage());
      return Optional.empty();
    }
  }

  /**
   * 解析作业状态字符串为 JobStatus 枚举
   *
   * @param state 状态字符串
   * @return JobStatus 枚举
   */
  private JobStatus parseJobStatus(String state) {
    if (state == null) {
      return JobStatus.UNKNOWN;
    }
    switch (state.toUpperCase()) {
      case "FINISHED":
        return JobStatus.FINISHED;
      case "FAILED":
        return JobStatus.FAILED;
      case "CANCELED":
        return JobStatus.CANCELED;
      case "RUNNING":
        return JobStatus.RUNNING;
      default:
        return JobStatus.UNKNOWN;
    }
  }

  /**
   * 解析 URL，处理尾部斜杠
   *
   * @param urlString URL 字符串
   * @return URL 对象
   */
  private URL parseUrl(String urlString) throws MalformedURLException {
    if (urlString.endsWith("/")) {
      urlString = urlString.substring(0, urlString.length() - 1);
    }
    return new URL(urlString);
  }
}
