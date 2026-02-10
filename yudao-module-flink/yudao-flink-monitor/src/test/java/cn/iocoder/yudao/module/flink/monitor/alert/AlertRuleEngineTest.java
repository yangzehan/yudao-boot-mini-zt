package cn.iocoder.yudao.module.flink.monitor.alert;

import cn.iocoder.yudao.module.flink.monitor.alert.engine.AlertRuleEngine;
import cn.iocoder.yudao.module.flink.monitor.alert.model.AlertConfig;
import cn.iocoder.yudao.module.flink.monitor.alert.model.AlertLevel;
import cn.iocoder.yudao.module.flink.monitor.alert.model.AlertMessage;
import cn.iocoder.yudao.module.flink.monitor.alert.model.AlertType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 告警规则引擎单元测试
 *
 * @author yzh
 */
class AlertRuleEngineTest {

    private AlertRuleEngine ruleEngine;
    private AlertMessage testAlertMessage;
    private AlertConfig testAlertConfig;

    @BeforeEach
    void setUp() {
        ruleEngine = new AlertRuleEngine();

        testAlertMessage = AlertMessage.builder()
                .alertId("test-001")
                .jobId("job-001")
                .jobName("TestFlinkJob")
                .type(AlertType.JOB_FAILED)
                .level(AlertLevel.CRITICAL)
                .occurredTime(LocalDateTime.now())
                .build();

        testAlertConfig = AlertConfig.builder()
                .enabled(true)
                .channels(Arrays.asList("mail"))
                .build();
    }

    @Test
    @DisplayName("测试正常规则评估 - 应该通过")
    void testNormalRuleEvaluation() {
        // Act
        AlertRuleEngine.RuleEvaluateResult result =
                ruleEngine.evaluate(testAlertMessage, testAlertConfig);

        // Assert
        assertTrue(result.isAllowed());
        assertNull(result.getReason());
    }

    @Test
    @DisplayName("测试全局告警禁用 - 应该拒绝")
    void testGlobalAlertDisabled() {
        // Arrange
        AlertConfig disabledConfig = AlertConfig.builder()
                .enabled(false)
                .channels(Arrays.asList("mail"))
                .build();

        // Act
        AlertRuleEngine.RuleEvaluateResult result =
                ruleEngine.evaluate(testAlertMessage, disabledConfig);

        // Assert
        assertFalse(result.isAllowed());
        assertEquals("全局告警已禁用", result.getReason());
    }

    @Test
    @DisplayName("测试空配置 - 应该拒绝")
    void testNullConfig() {
        // Act
        AlertRuleEngine.RuleEvaluateResult result =
                ruleEngine.evaluate(testAlertMessage, null);

        // Assert
        assertFalse(result.isAllowed());
        assertEquals("告警配置为空", result.getReason());
    }

    @Test
    @DisplayName("测试没有配置渠道 - 应该拒绝")
    void testNoChannelsConfigured() {
        // Arrange
        AlertConfig noChannelConfig = AlertConfig.builder()
                .enabled(true)
                .channels(null)
                .build();

        // Act
        AlertRuleEngine.RuleEvaluateResult result =
                ruleEngine.evaluate(testAlertMessage, noChannelConfig);

        // Assert
        assertFalse(result.isAllowed());
        assertEquals("没有配置告警渠道", result.getReason());
    }

    @Test
    @DisplayName("测试空渠道列表 - 应该拒绝")
    void testEmptyChannelsList() {
        // Arrange
        AlertConfig emptyChannelConfig = AlertConfig.builder()
                .enabled(true)
                .channels(Arrays.asList())
                .build();

        // Act
        AlertRuleEngine.RuleEvaluateResult result =
                ruleEngine.evaluate(testAlertMessage, emptyChannelConfig);

        // Assert
        assertFalse(result.isAllowed());
        assertEquals("没有配置告警渠道", result.getReason());
    }

    @Test
    @DisplayName("测试指定类型规则禁用")
    void testSpecificTypeRuleDisabled() {
        // Arrange
        AlertConfig disabledTypeConfig = AlertConfig.builder()
                .enabled(true)
                .channels(Arrays.asList("mail"))
                .rules(Arrays.asList(
                        AlertConfig.AlertRule.builder()
                                .type("JOB_FAILED")
                                .level("CRITICAL")
                                .enabled(false)
                                .build()
                ))
                .build();

        // Act
        AlertRuleEngine.RuleEvaluateResult result =
                ruleEngine.evaluate(testAlertMessage, disabledTypeConfig);

        // Assert
        assertFalse(result.isAllowed());
        assertEquals("告警类型 [JOB_FAILED] 已禁用", result.getReason());
    }

    @Test
    @DisplayName("测试获取指定类型规则")
    void testGetRuleByType() {
        // Arrange
        AlertConfig configWithRules = AlertConfig.builder()
                .enabled(true)
                .channels(Arrays.asList("mail"))
                .rules(Arrays.asList(
                        AlertConfig.AlertRule.builder()
                                .type("JOB_FAILED")
                                .level("CRITICAL")
                                .enabled(true)
                                .cooldownSeconds(300)
                                .build(),
                        AlertConfig.AlertRule.builder()
                                .type("JOB_CANCELLED")
                                .level("WARNING")
                                .enabled(true)
                                .cooldownSeconds(600)
                                .build()
                ))
                .build();

        // Act
        AlertConfig.AlertRule rule = configWithRules.getRuleByType("JOB_FAILED");

        // Assert
        assertNotNull(rule);
        assertEquals("JOB_FAILED", rule.getType());
        assertEquals("CRITICAL", rule.getLevel());
        assertEquals(300, rule.getCooldownSeconds());
    }

    @Test
    @DisplayName("测试获取不存在的类型规则")
    void testGetNonExistentTypeRule() {
        // Arrange
        AlertConfig configWithRules = AlertConfig.builder()
                .enabled(true)
                .channels(Arrays.asList("mail"))
                .rules(Arrays.asList(
                        AlertConfig.AlertRule.builder()
                                .type("JOB_FAILED")
                                .build()
                ))
                .build();

        // Act
        AlertConfig.AlertRule rule = configWithRules.getRuleByType("JOB_RESTART");

        // Assert
        assertNull(rule);
    }

    @Test
    @DisplayName("测试检查渠道是否启用")
    void testChannelEnabledCheck() {
        // Assert
        assertTrue(testAlertConfig.isChannelEnabled("mail"));
        assertFalse(testAlertConfig.isChannelEnabled("dingtalk"));
    }

    @Test
    @DisplayName("测试获取默认配置")
    void testGetDefaultConfig() {
        // Act
        AlertConfig defaultConfig = AlertConfig.getDefaultConfig();

        // Assert
        assertNotNull(defaultConfig);
        assertTrue(defaultConfig.getEnabled());
        assertNotNull(defaultConfig.getChannels());
        assertTrue(defaultConfig.getChannels().contains("mail"));
        assertNotNull(defaultConfig.getDedup());
        assertTrue(defaultConfig.getDedup().getEnabled());
    }
}
