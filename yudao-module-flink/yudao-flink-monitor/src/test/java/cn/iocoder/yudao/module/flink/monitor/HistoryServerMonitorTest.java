package cn.iocoder.yudao.module.flink.monitor;

import cn.iocoder.yudao.module.datastudio.api.enums.JobStatus;
import cn.iocoder.yudao.module.datastudio.api.job.dto.DataJobDto;
import cn.iocoder.yudao.module.flink.common.dto.HistoryJobInfo;
import cn.iocoder.yudao.module.flink.monitor.monitor.HistoryServerMonitor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link HistoryServerMonitor} 单元测试
 *
 * @author yzh
 */
class HistoryServerMonitorTest {

  private HistoryServerMonitor historyServerMonitor;

  private DataJobDto testJob;

  @BeforeEach
  void setUp() {
    historyServerMonitor = new HistoryServerMonitor("http://history-server:8082");

    Map<String, String> config = new HashMap<>();
    testJob = new DataJobDto();
    testJob.setConfig(config);
    testJob.setJobId("test-job-id");
    testJob.setWebUiUrl("http://flink:8081");
  }

  @Test
  void testIsEnabled_whenConfigured() {
    // When
    boolean enabled = historyServerMonitor.isEnabled();

    // Then
    assertTrue(enabled);
  }

  @Test
  void testIsEnabled_whenNotConfigured() {
    // Given
    HistoryServerMonitor monitor = new HistoryServerMonitor("");

    // When & Then
    assertFalse(monitor.isEnabled());
  }

  @Test
  void testGetJobStatus_whenDisabled() {
    // Given
    HistoryServerMonitor monitor = new HistoryServerMonitor("");

    // When
    JobStatus status = assertDoesNotThrow(() -> monitor.getJobStatus(testJob));

    // Then
    assertEquals(JobStatus.UNKNOWN, status);
  }

  @Test
  void testGetCompletedJobs_whenDisabled() {
    // Given
    HistoryServerMonitor monitor = new HistoryServerMonitor("");

    // When
    List<HistoryJobInfo> jobs = monitor.getCompletedJobs();

    // Then
    assertTrue(jobs.isEmpty());
  }

  @Test
  void testGetJobDetail_whenDisabled() {
    // Given
    HistoryServerMonitor monitor = new HistoryServerMonitor("");

    // When
    java.util.Optional<HistoryJobInfo> result = monitor.getJobDetail("test-job-id");

    // Then
    assertFalse(result.isPresent());
  }

  @Test
  void testGetHistoryJobInfo_whenDisabled() {
    // Given
    HistoryServerMonitor monitor = new HistoryServerMonitor("");

    // When
    HistoryJobInfo result = monitor.getHistoryJobInfo("test-job-id");

    // Then
    assertNull(result);
  }

  @Test
  void testParseJobStatus() throws Exception {
    // Given
    HistoryServerMonitor monitor = new HistoryServerMonitor("http://test:8082");

    // When & Then
    assertEquals(JobStatus.FINISHED, parseStatus(monitor, "FINISHED"));
    assertEquals(JobStatus.FAILED, parseStatus(monitor, "FAILED"));
    assertEquals(JobStatus.CANCELED, parseStatus(monitor, "CANCELED"));
    assertEquals(JobStatus.RUNNING, parseStatus(monitor, "RUNNING"));
    assertEquals(JobStatus.UNKNOWN, parseStatus(monitor, "UNKNOWN"));
    assertEquals(JobStatus.UNKNOWN, parseStatus(monitor, null));
    assertEquals(JobStatus.UNKNOWN, parseStatus(monitor, "INVALID_STATE"));
  }

  private JobStatus parseStatus(HistoryServerMonitor monitor, String state) throws Exception {
    java.lang.reflect.Method method = HistoryServerMonitor.class.getDeclaredMethod("parseJobStatus", String.class);
    method.setAccessible(true);
    return (JobStatus) method.invoke(monitor, state);
  }

  @Test
  void testJobStatusEnumValues() {
    // 验证 JobStatus 枚举包含所有预期状态
    assertNotNull(JobStatus.valueOf("FINISHED"));
    assertNotNull(JobStatus.valueOf("FAILED"));
    assertNotNull(JobStatus.valueOf("CANCELED"));
    assertNotNull(JobStatus.valueOf("RUNNING"));
    assertNotNull(JobStatus.valueOf("UNKNOWN"));
  }
}
