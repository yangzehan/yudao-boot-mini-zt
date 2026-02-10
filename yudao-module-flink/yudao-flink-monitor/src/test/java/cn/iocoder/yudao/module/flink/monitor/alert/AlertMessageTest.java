package cn.iocoder.yudao.module.flink.monitor.alert;

import cn.iocoder.yudao.module.flink.monitor.alert.model.AlertLevel;
import cn.iocoder.yudao.module.flink.monitor.alert.model.AlertMessage;
import cn.iocoder.yudao.module.flink.monitor.alert.model.AlertType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 告警消息模型单元测试
 *
 * @author yzh
 */
class AlertMessageTest {

    @Test
    @DisplayName("测试创建作业失败告警")
    void testCreateJobFailedAlert() {
        // Arrange
        String jobId = "job-001";
        String jobName = "TestJob";
        String errorMsg = "Task failed with exception";
        LocalDateTime occurredTime = LocalDateTime.now();

        // Act
        AlertMessage alert = AlertMessage.createJobFailedAlert(jobId, jobName, errorMsg, occurredTime);

        // Assert
        assertNotNull(alert.getAlertId());
        assertEquals(jobId, alert.getJobId());
        assertEquals(jobName, alert.getJobName());
        assertEquals(errorMsg, alert.getErrorMsg());
        assertEquals(occurredTime, alert.getOccurredTime());
        assertEquals(AlertType.JOB_FAILED, alert.getType());
        assertEquals(AlertLevel.CRITICAL, alert.getLevel());
        assertEquals(1, alert.getTriggerCount());
        assertFalse(alert.isRecovered());
        assertNotNull(alert.getTitle());
        assertTrue(alert.getTitle().contains(jobName));
        assertNotNull(alert.getContent());
    }

    @Test
    @DisplayName("测试去重键生成")
    void testDedupKeyGeneration() {
        // Arrange
        AlertMessage alert = AlertMessage.builder()
                .jobId("job-001")
                .type(AlertType.JOB_FAILED)
                .level(AlertLevel.CRITICAL)
                .build();

        // Act
        String dedupKey = alert.getDedupKey();

        // Assert
        assertEquals("job-001:JOB_FAILED:CRITICAL", dedupKey);
    }

    @Test
    @DisplayName("测试严重告警判断")
    void testCriticalAlertJudgment() {
        // Arrange
        AlertMessage criticalAlert = AlertMessage.builder()
                .level(AlertLevel.CRITICAL)
                .build();

        AlertMessage warningAlert = AlertMessage.builder()
                .level(AlertLevel.WARNING)
                .build();

        AlertMessage infoAlert = AlertMessage.builder()
                .level(AlertLevel.INFO)
                .build();

        // Assert
        assertTrue(criticalAlert.isCritical());
        assertFalse(warningAlert.isCritical());
        assertFalse(infoAlert.isCritical());
    }

    @Test
    @DisplayName("测试添加扩展参数")
    void testAddExtendParams() {
        // Arrange
        AlertMessage alert = new AlertMessage();

        // Act
        alert.addExtendParam("key1", "value1");
        alert.addExtendParam("key2", 123);
        alert.addExtendParam("key3", true);

        // Assert
        assertNotNull(alert.getExtendParams());
        assertEquals("value1", alert.getExtendParams().get("key1"));
        assertEquals(123, alert.getExtendParams().get("key2"));
        assertEquals(true, alert.getExtendParams().get("key3"));
    }

    @Test
    @DisplayName("测试添加扩展参数链式调用")
    void testAddExtendParamsChainCall() {
        // Arrange
        AlertMessage alert = new AlertMessage();

        // Act
        AlertMessage result = alert
                .addExtendParam("key1", "value1")
                .addExtendParam("key2", 123);

        // Assert
        assertSame(alert, result);
        assertEquals(2, alert.getExtendParams().size());
    }

    @Test
    @DisplayName("测试空错误信息的处理")
    void testNullErrorMessageHandling() {
        // Act
        AlertMessage alert = AlertMessage.createJobFailedAlert("job-001", "TestJob", null, LocalDateTime.now());

        // Assert
        assertNotNull(alert.getErrorMsg());
        assertEquals("未知错误", alert.getErrorMsg());
    }

    @Test
    @DisplayName("测试构建器模式创建消息")
    void testBuilderPattern() {
        // Arrange & Act
        AlertMessage alert = AlertMessage.builder()
                .alertId("custom-id")
                .jobId("job-002")
                .jobName("BuildTest")
                .clusterId(100L)
                .type(AlertType.JOB_RESTART)
                .level(AlertLevel.INFO)
                .errorMsg("Restarting")
                .occurredTime(LocalDateTime.now())
                .receiverIds(java.util.Arrays.asList(1L, 2L, 3L))
                .receiverEmails(java.util.Arrays.asList("a@test.com", "b@test.com"))
                .extendParams(new HashMap<>())
                .sourceMonitor("UnifiedFlinkMonitor")
                .alertConfigId(10L)
                .recovered(true)
                .triggerCount(5)
                .createTime(LocalDateTime.now())
                .build();

        // Assert
        assertEquals("custom-id", alert.getAlertId());
        assertEquals("job-002", alert.getJobId());
        assertEquals("BuildTest", alert.getJobName());
        assertEquals(100L, alert.getClusterId());
        assertEquals(AlertType.JOB_RESTART, alert.getType());
        assertEquals(AlertLevel.INFO, alert.getLevel());
        assertEquals("Restarting", alert.getErrorMsg());
        assertEquals(3, alert.getReceiverIds().size());
        assertEquals(2, alert.getReceiverEmails().size());
        assertEquals("UnifiedFlinkMonitor", alert.getSourceMonitor());
        assertEquals(10L, alert.getAlertConfigId());
        assertTrue(alert.isRecovered());
        assertEquals(5, alert.getTriggerCount());
    }

    @Test
    @DisplayName("测试 AlertLevel 比较")
    void testAlertLevelComparison() {
        assertTrue(AlertLevel.CRITICAL.isHigherThan(AlertLevel.WARNING));
        assertTrue(AlertLevel.CRITICAL.isHigherThan(AlertLevel.INFO));
        assertTrue(AlertLevel.WARNING.isHigherThan(AlertLevel.INFO));
        assertFalse(AlertLevel.INFO.isHigherThan(AlertLevel.WARNING));
        assertFalse(AlertLevel.CRITICAL.isHigherThan(AlertLevel.CRITICAL));
    }
}
