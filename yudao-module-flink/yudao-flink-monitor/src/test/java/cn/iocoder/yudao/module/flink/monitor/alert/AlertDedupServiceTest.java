package cn.iocoder.yudao.module.flink.monitor.alert;

import cn.iocoder.yudao.module.flink.monitor.alert.dedup.AlertDedupService;
import cn.iocoder.yudao.module.flink.monitor.alert.model.AlertConfig;
import cn.iocoder.yudao.module.flink.monitor.alert.model.AlertLevel;
import cn.iocoder.yudao.module.flink.monitor.alert.model.AlertMessage;
import cn.iocoder.yudao.module.flink.monitor.alert.model.AlertType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 告警去重服务单元测试
 *
 * @author yzh
 */
@ExtendWith(MockitoExtension.class)
class AlertDedupServiceTest {

    @Mock
    private RedisTemplate<Object, Object> redisTemplate;

    @Mock
    private ValueOperations<Object, Object> valueOperations;

    private AlertDedupService alertDedupService;

    private AlertMessage testAlertMessage;
    private AlertConfig testAlertConfig;

    @BeforeEach
    void setUp() throws Exception {
        alertDedupService = new AlertDedupService();
        // 使用反射注入 mock
        var field = AlertDedupService.class.getDeclaredField("redisTemplate");
        field.setAccessible(true);
        field.set(alertDedupService, redisTemplate);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        testAlertMessage = AlertMessage.builder()
                .alertId("test-001")
                .jobId("job-001")
                .type(AlertType.JOB_FAILED)
                .level(AlertLevel.CRITICAL)
                .occurredTime(LocalDateTime.now())
                .build();

        testAlertConfig = AlertConfig.builder()
                .enabled(true)
                .dedup(AlertConfig.DedupConfig.builder()
                        .enabled(true)
                        .windowSeconds(1800)
                        .maxCount(3)
                        .build())
                .build();
    }

    @Test
    @DisplayName("测试首次触发告警 - 应该允许")
    void testFirstTriggerShouldAllow() {
        // Arrange
        when(valueOperations.get(anyString())).thenReturn(null);

        // Act
        AlertDedupService.DedupCheckResult result =
                alertDedupService.check(testAlertMessage, testAlertConfig);

        // Assert
        assertTrue(result.isAllowed());
        verify(valueOperations).set(
                eq("flink:alert:dedup:job-001:JOB_FAILED:CRITICAL"),
                anyString(),
                eq(Duration.ofSeconds(1800)));
    }

    @Test
    @DisplayName("测试去重窗口内重复触发 - 应该拒绝")
    void testRepeatedTriggerInWindowShouldDeny() {
        // Arrange
        when(valueOperations.get(
                eq("flink:alert:dedup:job-001:JOB_FAILED:CRITICAL"))).thenReturn("existing-id");
        when(valueOperations.get(
                eq("flink:alert:count:job-001:JOB_FAILED:CRITICAL"))).thenReturn("3");

        // Act
        AlertDedupService.DedupCheckResult result =
                alertDedupService.check(testAlertMessage, testAlertConfig);

        // Assert
        assertFalse(result.isAllowed());
        assertEquals("超过最大告警次数(3)", result.getReason());
    }

    @Test
    @DisplayName("测试计数递增")
    void testCountIncrement() {
        // Arrange
        when(valueOperations.get(
                eq("flink:alert:dedup:job-001:JOB_FAILED:CRITICAL"))).thenReturn("existing-id");
        when(valueOperations.get(
                eq("flink:alert:count:job-001:JOB_FAILED:CRITICAL"))).thenReturn("1");
        when(redisTemplate.getExpire(anyString(), any())).thenReturn(1000L);

        // Act
        AlertDedupService.DedupCheckResult result =
                alertDedupService.check(testAlertMessage, testAlertConfig);

        // Assert
        assertTrue(result.isAllowed());
        verify(valueOperations).increment("flink:alert:count:job-001:JOB_FAILED:CRITICAL");
    }

    @Test
    @DisplayName("测试去重禁用 - 应该允许")
    void testDedupDisabledShouldAllow() {
        // Arrange
        AlertConfig disabledConfig = AlertConfig.builder()
                .enabled(true)
                .dedup(AlertConfig.DedupConfig.builder()
                        .enabled(false)
                        .build())
                .build();

        // Act
        AlertDedupService.DedupCheckResult result =
                alertDedupService.check(testAlertMessage, disabledConfig);

        // Assert
        assertTrue(result.isAllowed());
        verify(valueOperations, never()).set(anyString(), any(), any(Duration.class));
    }

    @Test
    @DisplayName("测试 Redis 异常时允许发送")
    void testRedisExceptionShouldAllow() {
        // Arrange
        when(valueOperations.get(anyString())).thenThrow(
                new RuntimeException("Redis 连接失败"));

        // Act
        AlertDedupService.DedupCheckResult result =
                alertDedupService.check(testAlertMessage, testAlertConfig);

        // Assert
        assertTrue(result.isAllowed()); // 异常时允许发送，避免阻塞
    }

    @Test
    @DisplayName("测试清除去重状态")
    void testClearDedupStatus() {
        // Act
        alertDedupService.clear(testAlertMessage);

        // Assert
        verify(redisTemplate).delete("flink:alert:dedup:job-001:JOB_FAILED:CRITICAL");
        verify(redisTemplate).delete("flink:alert:count:job-001:JOB_FAILED:CRITICAL");
    }

    @Test
    @DisplayName("测试空消息检查")
    void testNullMessageCheck() {
        // Act
        AlertDedupService.DedupCheckResult result =
                alertDedupService.check(null, testAlertConfig);

        // Assert
        assertTrue(result.isAllowed());
    }

    @Test
    @DisplayName("测试空配置检查")
    void testNullConfigCheck() {
        // Act
        AlertDedupService.DedupCheckResult result =
                alertDedupService.check(testAlertMessage, null);

        // Assert
        assertTrue(result.isAllowed());
    }

    @Test
    @DisplayName("测试空配置的去重配置检查")
    void testNullDedupConfigCheck() {
        // Arrange
        AlertConfig nullDedupConfig = AlertConfig.builder()
                .enabled(true)
                .dedup(null)
                .build();

        // Act
        AlertDedupService.DedupCheckResult result =
                alertDedupService.check(testAlertMessage, nullDedupConfig);

        // Assert
        assertTrue(result.isAllowed());
    }

    @Test
    @DisplayName("测试去重状态获取")
    void testGetStatus() {
        // Arrange
        when(valueOperations.get(
                eq("flink:alert:dedup:job-001:JOB_FAILED:CRITICAL"))).thenReturn("existing-id");
        when(valueOperations.get(
                eq("flink:alert:count:job-001:JOB_FAILED:CRITICAL"))).thenReturn("2");
        when(redisTemplate.getExpire(anyString(), any())).thenReturn(500L);

        // Act
        AlertDedupService.DedupStatus status =
                alertDedupService.getStatus(testAlertMessage);

        // Assert
        assertNotNull(status);
        assertTrue(status.isInWindow());
        assertEquals(2, status.getTriggerCount());
    }

    @Test
    @DisplayName("测试空消息获取状态")
    void testGetStatusWithNullMessage() {
        // Act
        AlertDedupService.DedupStatus status =
                alertDedupService.getStatus(null);

        // Assert
        assertNotNull(status);
        assertFalse(status.isInWindow());
        assertEquals(0, status.getTriggerCount());
    }
}
