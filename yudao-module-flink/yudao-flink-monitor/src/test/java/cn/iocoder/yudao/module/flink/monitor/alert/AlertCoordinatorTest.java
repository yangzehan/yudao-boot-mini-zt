package cn.iocoder.yudao.module.flink.monitor.alert;

import cn.iocoder.yudao.module.flink.monitor.alert.channel.AlertChannel;
import cn.iocoder.yudao.module.flink.monitor.alert.channel.AlertChannelManager;
import cn.iocoder.yudao.module.flink.monitor.alert.channel.AlertSendResult;
import cn.iocoder.yudao.module.flink.monitor.alert.dedup.AlertDedupService;
import cn.iocoder.yudao.module.flink.monitor.alert.engine.AlertRuleEngine;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 告警协调器单元测试
 *
 * @author yzh
 */
@ExtendWith(MockitoExtension.class)
class AlertCoordinatorTest {

    @Mock
    private AlertRuleEngine ruleEngine;

    @Mock
    private AlertDedupService dedupService;

    @Mock
    private AlertChannelManager channelManager;

    private AlertCoordinator alertCoordinator;

    private AlertMessage testAlertMessage;
    private AlertConfig testAlertConfig;

    @BeforeEach
    void setUp() {
        alertCoordinator = new AlertCoordinator(ruleEngine, dedupService, channelManager);

        // 创建测试告警消息
        testAlertMessage = AlertMessage.builder()
                .alertId("test-alert-001")
                .jobId("job-001")
                .jobName("TestFlinkJob")
                .type(AlertType.JOB_FAILED)
                .level(AlertLevel.CRITICAL)
                .errorMsg("Task execution failed")
                .occurredTime(LocalDateTime.now())
                .receiverEmails(Arrays.asList("test@example.com"))
                .build();

        // 创建测试告警配置
        testAlertConfig = AlertConfig.builder()
                .enabled(true)
                .channels(Arrays.asList("mail"))
                .build();
    }

    @Test
    @DisplayName("测试正常告警处理流程")
    void testHandleNormalAlert() {
        // Arrange
        when(ruleEngine.evaluate(any(), any())).thenReturn(
                AlertRuleEngine.RuleEvaluateResult.allow(null));
        when(dedupService.check(any(), any())).thenReturn(
                AlertDedupService.DedupCheckResult.allow());

        AlertChannel mockChannel = createMockChannel();
        when(channelManager.getEnabledChannels(any())).thenReturn(
                Arrays.asList(mockChannel));

        // Act
        AlertCoordinator.AlertHandleResult result = alertCoordinator.handle(
                testAlertMessage, testAlertConfig);

        // Assert
        assertTrue(result.isSuccess());
        assertTrue(result.isAllowed());
        assertNotNull(result.getChannelResults());
        assertEquals(1, result.getChannelResults().size());
        verify(ruleEngine).evaluate(testAlertMessage, testAlertConfig);
        verify(dedupService).check(testAlertMessage, testAlertConfig);
        verify(mockChannel).send(testAlertMessage, testAlertConfig);
    }

    @Test
    @DisplayName("测试规则评估不通过")
    void testHandleAlertBlockedByRule() {
        // Arrange
        when(ruleEngine.evaluate(any(), any())).thenReturn(
                AlertRuleEngine.RuleEvaluateResult.deny("告警功能未启用"));

        // Act
        AlertCoordinator.AlertHandleResult result = alertCoordinator.handle(
                testAlertMessage, testAlertConfig);

        // Assert
        assertTrue(result.isSuccess());
        assertFalse(result.isAllowed());
        assertEquals("告警功能未启用", result.getBlockedReason());
        verify(ruleEngine).evaluate(testAlertMessage, testAlertConfig);
        verify(dedupService, never()).check(any(), any());
    }

    @Test
    @DisplayName("测试去重拦截")
    void testHandleAlertBlockedByDedup() {
        // Arrange
        when(ruleEngine.evaluate(any(), any())).thenReturn(
                AlertRuleEngine.RuleEvaluateResult.allow(null));
        when(dedupService.check(any(), any())).thenReturn(
                AlertDedupService.DedupCheckResult.deny("超过最大告警次数(3)", 3));

        // Act
        AlertCoordinator.AlertHandleResult result = alertCoordinator.handle(
                testAlertMessage, testAlertConfig);

        // Assert
        assertTrue(result.isSuccess());
        assertFalse(result.isAllowed());
        assertEquals("去重: 超过最大告警次数(3)", result.getBlockedReason());
        assertEquals(3, result.getDedupTriggerCount());
    }

    @Test
    @DisplayName("测试没有可用渠道")
    void testHandleAlertNoAvailableChannel() {
        // Arrange
        when(ruleEngine.evaluate(any(), any())).thenReturn(
                AlertRuleEngine.RuleEvaluateResult.allow(null));
        when(dedupService.check(any(), any())).thenReturn(
                AlertDedupService.DedupCheckResult.allow());
        when(channelManager.getEnabledChannels(any())).thenReturn(
                Collections.emptyList());

        // Act
        AlertCoordinator.AlertHandleResult result = alertCoordinator.handle(
                testAlertMessage, testAlertConfig);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("没有可用的告警渠道", result.getMessage());
    }

    @Test
    @DisplayName("测试多个渠道发送")
    void testHandleAlertMultipleChannels() {
        // Arrange
        when(ruleEngine.evaluate(any(), any())).thenReturn(
                AlertRuleEngine.RuleEvaluateResult.allow(null));
        when(dedupService.check(any(), any())).thenReturn(
                AlertDedupService.DedupCheckResult.allow());

        AlertChannel successChannel = createMockChannel("success-channel",
                AlertSendResult.success("msg-001"));
        AlertChannel failChannel = createMockChannel("fail-channel",
                AlertSendResult.failure("发送失败"));

        when(channelManager.getEnabledChannels(any())).thenReturn(
                Arrays.asList(successChannel, failChannel));

        // Act
        AlertCoordinator.AlertHandleResult result = alertCoordinator.handle(
                testAlertMessage, testAlertConfig);

        // Assert
        assertTrue(result.isSuccess()); // 只要有一个成功就算成功
        assertEquals(2, result.getChannelResults().size());
        assertEquals(1, result.getChannelResults().stream()
                .filter(r -> r.isSuccess()).count());
    }

    @Test
    @DisplayName("测试异常处理")
    void testHandleAlertException() {
        // Arrange
        when(ruleEngine.evaluate(any(), any())).thenThrow(
                new RuntimeException("测试异常"));

        // Act
        AlertCoordinator.AlertHandleResult result = alertCoordinator.handle(
                testAlertMessage, testAlertConfig);

        // Assert
        assertFalse(result.isSuccess());
        assertNotNull(result.getErrorMsg());
        assertTrue(result.getErrorMsg().contains("测试异常"));
    }

    // ==================== 辅助方法 ====================

    private AlertChannel createMockChannel() {
        return createMockChannel("test-channel", AlertSendResult.success());
    }

    private AlertChannel createMockChannel(String channelId, AlertSendResult result) {
        return new AlertChannel() {
            @Override
            public String getChannelId() {
                return channelId;
            }

            @Override
            public String getChannelName() {
                return "Test Channel";
            }

            @Override
            public AlertSendResult send(AlertMessage message, AlertConfig config) {
                return result;
            }
        };
    }
}
