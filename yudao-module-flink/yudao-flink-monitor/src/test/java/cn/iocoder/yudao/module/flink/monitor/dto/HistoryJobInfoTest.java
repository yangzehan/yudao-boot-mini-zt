package cn.iocoder.yudao.module.flink.monitor.dto;

import cn.iocoder.yudao.module.flink.common.dto.HistoryJobInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link HistoryJobInfo} 单元测试
 *
 * @author yzh
 */
class HistoryJobInfoTest {

  @Test
  void testBuilderAndGetters() {
    // Given
    String jobId = "job-001";
    String jobName = "TestJob";
    String state = "FINISHED";
    long startTime = 1000L;
    long endTime = 5000L;
    long duration = 4000L;
    int parallelism = 4;
    String lastError = null;

    // When
    HistoryJobInfo historyJobInfo = HistoryJobInfo.builder()
        .jobId(jobId)
        .jobName(jobName)
        .state(state)
        .startTime(startTime)
        .endTime(endTime)
        .duration(duration)
        .parallelism(parallelism)
        .lastError(lastError)
        .build();

    // Then
    assertEquals(jobId, historyJobInfo.getJobId());
    assertEquals(jobName, historyJobInfo.getJobName());
    assertEquals(state, historyJobInfo.getState());
    assertEquals(startTime, historyJobInfo.getStartTime());
    assertEquals(endTime, historyJobInfo.getEndTime());
    assertEquals(duration, historyJobInfo.getDuration());
    assertEquals(parallelism, historyJobInfo.getParallelism());
    assertNull(historyJobInfo.getLastError());
  }

  @Test
  void testSetters() {
    // Given
    HistoryJobInfo historyJobInfo = HistoryJobInfo.builder().build();

    // When
    historyJobInfo.setJobId("job-002");
    historyJobInfo.setJobName("AnotherJob");
    historyJobInfo.setState("FAILED");
    historyJobInfo.setStartTime(2000L);
    historyJobInfo.setEndTime(3000L);
    historyJobInfo.setDuration(1000L);
    historyJobInfo.setParallelism(8);
    historyJobInfo.setLastError("Test error");

    // Then
    assertEquals("job-002", historyJobInfo.getJobId());
    assertEquals("AnotherJob", historyJobInfo.getJobName());
    assertEquals("FAILED", historyJobInfo.getState());
    assertEquals(2000L, historyJobInfo.getStartTime());
    assertEquals(3000L, historyJobInfo.getEndTime());
    assertEquals(1000L, historyJobInfo.getDuration());
    assertEquals(8, historyJobInfo.getParallelism());
    assertEquals("Test error", historyJobInfo.getLastError());
  }

  @Test
  void testAllStates() {
    // Given & When & Then
    String[] validStates = {"FINISHED", "FAILED", "CANCELED", "RUNNING"};

    for (String state : validStates) {
      HistoryJobInfo historyJobInfo = HistoryJobInfo.builder()
          .state(state)
          .build();
      assertEquals(state, historyJobInfo.getState());
    }
  }
}
