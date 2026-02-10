package cn.iocoder.yudao.module.flink.monitor.alert;

import cn.iocoder.yudao.module.flink.monitor.alert.channel.AlertChannel;
import cn.iocoder.yudao.module.flink.monitor.alert.channel.AlertChannelManager;
import cn.iocoder.yudao.module.flink.monitor.alert.channel.AlertSendResult;
import cn.iocoder.yudao.module.flink.monitor.alert.dedup.AlertDedupService;
import cn.iocoder.yudao.module.flink.monitor.alert.engine.AlertRuleEngine;
import cn.iocoder.yudao.module.flink.monitor.alert.model.AlertConfig;
import cn.iocoder.yudao.module.flink.monitor.alert.model.AlertMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 告警协调器
 *
 * <p>告警处理的中央协调器，负责:</p>
 * <ul>
 *   <li>接收告警消息</li>
 *   <li>执行规则评估</li>
 *   <li>执行去重检查</li>
 *   <li>分发到各告警渠道</li>
 *   <li>汇总发送结果</li>
 * </ul>
 *
 * <p>采用策略模式 + 责任链模式的组合:</p>
 * <ul>
 *   <li>策略模式: AlertRuleEngine, AlertDedupService</li>
 *   <li>责任链: AlertChannelManager.getEnabledChannels()</li>
 * </ul>
 *
 * @author yzh
 */
@Slf4j
@Service
public class AlertCoordinator {

    @Resource
    private AlertRuleEngine ruleEngine;

    @Resource
    private AlertDedupService dedupService;

    @Resource
    private AlertChannelManager channelManager;

    /**
     * 处理告警
     *
     * @param message 告警消息
     * @param config  告警配置
     * @return 处理结果
     */
    public AlertHandleResult handle(AlertMessage message, AlertConfig config) {
        log.info("开始处理告警: jobId={}, jobName={}, type={}, level={}",
                message.getJobId(), message.getJobName(), message.getType(), message.getLevel());

        long startTime = System.currentTimeMillis();

        try {
            // Step 1: 规则评估
            AlertRuleEngine.RuleEvaluateResult ruleResult = ruleEngine.evaluate(message, config);
            if (!ruleResult.isAllowed()) {
                log.info("告警被规则拦截: jobId={}, reason={}", message.getJobId(), ruleResult.getReason());
                return AlertHandleResult.builder()
                        .success(true)
                        .message("告警处理完成")
                        .allowed(false)
                        .blockedReason(ruleResult.getReason())
                        .build();
            }

            // Step 2: 去重检查
            AlertDedupService.DedupCheckResult dedupResult = dedupService.check(message, config);
            if (!dedupResult.isAllowed()) {
                log.info("告警被去重拦截: jobId={}, reason={}", message.getJobId(), dedupResult.getReason());
                return AlertHandleResult.builder()
                        .success(true)
                        .message("告警处理完成")
                        .allowed(false)
                        .blockedReason("去重: " + dedupResult.getReason())
                        .dedupTriggerCount(dedupResult.getCurrentCount())
                        .build();
            }

            // Step 3: 获取启用的渠道
            List<AlertChannel> channels = channelManager.getEnabledChannels(config);
            if (channels.isEmpty()) {
                log.warn("没有可用的告警渠道: jobId={}", message.getJobId());
                return AlertHandleResult.builder()
                        .success(false)
                        .message("没有可用的告警渠道")
                        .build();
            }

            // Step 4: 发送到各渠道
            List<ChannelSendResult> channelResults = new ArrayList<>();
            for (AlertChannel channel : channels) {
                try {
                    AlertSendResult sendResult = channel.send(message, config);
                    channelResults.add(ChannelSendResult.builder()
                            .channelId(channel.getChannelId())
                            .channelName(channel.getChannelName())
                            .success(sendResult.isSuccess())
                            .messageId(sendResult.getMessageId())
                            .errorMsg(sendResult.getErrorMsg())
                            .build());

                    if (!sendResult.isSuccess()) {
                        log.warn("渠道发送失败: jobId={}, channel={}, error={}",
                                message.getJobId(), channel.getChannelId(), sendResult.getErrorMsg());
                    }
                } catch (Exception e) {
                    log.error("渠道发送异常: jobId={}, channel={}", message.getJobId(), channel.getChannelId(), e);
                    channelResults.add(ChannelSendResult.builder()
                            .channelId(channel.getChannelId())
                            .channelName(channel.getChannelName())
                            .success(false)
                            .errorMsg(e.getMessage())
                            .build());
                }
            }

            // Step 5: 判断整体结果
            boolean allSuccess = channelResults.stream().allMatch(ChannelSendResult::isSuccess);
            boolean anySuccess = channelResults.stream().anyMatch(ChannelSendResult::isSuccess);

            long costTime = System.currentTimeMillis() - startTime;

            log.info("告警处理完成: jobId={}, success={}, channels={}, cost={}ms",
                    message.getJobId(), allSuccess, channelResults.size(), costTime);

            return AlertHandleResult.builder()
                    .success(anySuccess) // 只要有一个渠道成功就算成功
                    .message(allSuccess ? "所有渠道发送成功" : "部分渠道发送失败")
                    .allowed(true)
                    .channelResults(channelResults)
                    .dedupTriggerCount(dedupResult.getCurrentCount())
                    .costTimeMs(costTime)
                    .build();

        } catch (Exception e) {
            log.error("告警处理异常: jobId={}", message.getJobId(), e);
            return AlertHandleResult.builder()
                    .success(false)
                    .message("告警处理异常: " + e.getMessage())
                    .errorMsg(e.getMessage())
                    .build();
        }
    }

    /**
     * 处理恢复通知
     */
    public AlertHandleResult handleRecovery(AlertMessage message, AlertConfig config) {
        log.info("开始处理恢复通知: jobId={}, jobName={}", message.getJobId(), message.getJobName());

        try {
            // 清除去重状态
            dedupService.clear(message);

            // 发送到各渠道
            List<AlertChannel> channels = channelManager.getEnabledChannels(config);
            List<ChannelSendResult> channelResults = new ArrayList<>();

            for (AlertChannel channel : channels) {
                try {
                    AlertSendResult sendResult = channel.sendRecovery(message, config);
                    channelResults.add(ChannelSendResult.builder()
                            .channelId(channel.getChannelId())
                            .channelName(channel.getChannelName())
                            .success(sendResult.isSuccess())
                            .messageId(sendResult.getMessageId())
                            .errorMsg(sendResult.getErrorMsg())
                            .build());
                } catch (Exception e) {
                    channelResults.add(ChannelSendResult.builder()
                            .channelId(channel.getChannelId())
                            .channelName(channel.getChannelName())
                            .success(false)
                            .errorMsg(e.getMessage())
                            .build());
                }
            }

            return AlertHandleResult.builder()
                    .success(channelResults.stream().anyMatch(ChannelSendResult::isSuccess))
                    .message("恢复通知发送完成")
                    .channelResults(channelResults)
                    .build();

        } catch (Exception e) {
            log.error("恢复通知处理异常: jobId={}", message.getJobId(), e);
            return AlertHandleResult.builder()
                    .success(false)
                    .message("恢复通知处理异常: " + e.getMessage())
                    .errorMsg(e.getMessage())
                    .build();
        }
    }

    // ==================== 内部类 ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlertHandleResult {

        /**
         * 是否成功
         */
        private boolean success;

        /**
         * 结果消息
         */
        private String message;

        /**
         * 是否被允许发送
         */
        private boolean allowed;

        /**
         * 拦截原因
         */
        private String blockedReason;

        /**
         * 去重触发次数
         */
        private Integer dedupTriggerCount;

        /**
         * 渠道发送结果
         */
        private List<ChannelSendResult> channelResults;

        /**
         * 耗时(毫秒)
         */
        private Long costTimeMs;

        /**
         * 异常信息
         */
        private String errorMsg;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChannelSendResult {

        /**
         * 渠道ID
         */
        private String channelId;

        /**
         * 渠道名称
         */
        private String channelName;

        /**
         * 是否成功
         */
        private boolean success;

        /**
         * 消息ID
         */
        private String messageId;

        /**
         * 错误信息
         */
        private String errorMsg;
    }
}
