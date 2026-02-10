package cn.iocoder.yudao.module.flink.monitor.alert.engine;

import cn.iocoder.yudao.module.flink.monitor.alert.model.AlertConfig;
import cn.iocoder.yudao.module.flink.monitor.alert.model.AlertLevel;
import cn.iocoder.yudao.module.flink.monitor.alert.model.AlertMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 告警规则引擎
 *
 * <p>负责评估告警规则:</p>
 * <ul>
 *   <li>告警级别匹配</li>
 *   <li>静默期检查</li>
 *   <li>冷却时间检查</li>
 *   <li>渠道过滤</li>
 * </ul>
 *
 * @author yzh
 */
@Slf4j
@Component
public class AlertRuleEngine {

    /**
     * 评估告警规则
     *
     * @param message 告警消息
     * @param config  告警配置
     * @return 规则评估结果
     */
    public RuleEvaluateResult evaluate(AlertMessage message, AlertConfig config) {
        if (config == null) {
            return RuleEvaluateResult.deny("告警配置为空");
        }

        // 1. 检查全局启用状态
        if (!Boolean.TRUE.equals(config.getEnabled())) {
            return RuleEvaluateResult.deny("全局告警已禁用");
        }

        // 2. 获取告警类型对应的规则
        AlertConfig.AlertRule rule = config.getRuleByType(message.getType().name());
        if (rule != null && !Boolean.TRUE(rule.getEnabled())) {
            return RuleEvaluateResult.deny(String.format("告警类型 [%s] 已禁用", message.getType().name()));
        }

        // 3. 检查静默期
        if (isInSilencePeriod(config)) {
            return RuleEvaluateResult.deny("当前处于静默期");
        }

        // 4. 检查告警级别
        if (!isLevelEnabled(message.getLevel(), config)) {
            return RuleEvaluateResult.deny(String.format("告警级别 [%s] 未启用", message.getLevel().name()));
        }

        // 5. 检查是否有启用的渠道
        List<String> enabledChannels = config.getChannels();
        if (enabledChannels == null || enabledChannels.isEmpty()) {
            return RuleEvaluateResult.deny("没有配置告警渠道");
        }

        log.debug("告警规则评估通过: jobId={}, type={}, level={}",
                message.getJobId(), message.getType(), message.getLevel());

        return RuleEvaluateResult.allow(rule);
    }

    /**
     * 检查是否处于静默期
     */
    private boolean isInSilencePeriod(AlertConfig config) {
        AlertConfig.SilenceConfig silenceConfig = config.getSilence();
        if (silenceConfig == null || !Boolean.TRUE.equals(silenceConfig.getEnabled())) {
            return false;
        }

        // 检查静默级别
        List<String> silenceLevels = silenceConfig.getLevels();
        if (silenceLevels != null && !silenceLevels.isEmpty()) {
            // 静默配置了指定级别，当前级别不在其中则不静默
            // 这里简化处理，实际应该根据具体需求调整
        }

        // 检查时间范围
        String startTime = silenceConfig.getStartTime();
        String endTime = silenceConfig.getEndTime();

        if (startTime != null && endTime != null) {
            LocalTime now = LocalTime.now();
            LocalTime start = LocalTime.parse(startTime);
            LocalTime end = LocalTime.parse(endTime);

            // 处理跨天情况
            if (start.isBefore(end)) {
                return !now.isBefore(start) && !now.isAfter(end);
            } else {
                // 跨天: 22:00 - 06:00
                return !now.isBefore(start) || !now.isAfter(end);
            }
        }

        return false;
    }

    /**
     * 检查告警级别是否启用
     */
    private boolean isLevelEnabled(AlertLevel level, AlertConfig config) {
        // 简单处理：所有级别默认启用
        // 实际可以从配置中读取启用的级别列表
        return true;
    }

    // ==================== 内部类 ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RuleEvaluateResult {

        /**
         * 是否允许
         */
        private boolean allowed;

        /**
         * 原因
         */
        private String reason;

        /**
         * 匹配的规则
         */
        private AlertConfig.AlertRule matchedRule;

        public static RuleEvaluateResult allow(AlertConfig.AlertRule rule) {
            return RuleEvaluateResult.builder()
                    .allowed(true)
                    .reason(null)
                    .matchedRule(rule)
                    .build();
        }

        public static RuleEvaluateResult deny(String reason) {
            return RuleEvaluateResult.builder()
                    .allowed(false)
                    .reason(reason)
                    .matchedRule(null)
                    .build();
        }
    }
}
