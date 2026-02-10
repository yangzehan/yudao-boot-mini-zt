package cn.iocoder.yudao.module.flink.monitor.alert.channel;

import cn.iocoder.yudao.module.flink.monitor.alert.model.AlertMessage;
import cn.iocoder.yudao.module.flink.monitor.alert.model.AlertConfig;

/**
 * 告警渠道接口 (SPI 扩展点)
 *
 * <p>所有告警渠道必须实现此接口</p>
 * <p>支持通过 Java SPI 机制自动发现和加载</p>
 *
 * <p>实现示例:</p>
 * <ul>
 *   <li>MailAlertChannel - 邮件告警渠道</li>
 *   <li>DingTalkAlertChannel - 钉钉告警渠道</li>
 *   <li>WeChatAlertChannel - 企业微信告警渠道</li>
 *   <li>SmsAlertChannel - 短信告警渠道</li>
 *   <li>WebhookAlertChannel - Webhook 告警渠道</li>
 * </ul>
 *
 * @author yzh
 */
public interface AlertChannel {

    /**
     * 获取渠道唯一标识
     *
     * @return 渠道ID (如: mail, dingtalk, webhook)
     */
    String getChannelId();

    /**
     * 获取渠道名称
     *
     * @return 渠道名称
     */
    String getChannelName();

    /**
     * 获取渠道优先级 (数值越小优先级越高)
     *
     * @return 优先级
     */
    default int getPriority() {
        return 100;
    }

    /**
     * 发送告警消息
     *
     * @param message 告警消息
     * @param config  告警配置
     * @return 发送结果
     */
    AlertSendResult send(AlertMessage message, AlertConfig config);

    /**
     * 发送恢复通知
     *
     * @param message 原始告警消息
     * @param config  告警配置
     * @return 发送结果
     */
    default AlertSendResult sendRecovery(AlertMessage message, AlertConfig config) {
        // 默认不发送恢复通知，子类可覆盖
        return AlertSendResult.success();
    }

    /**
     * 检查渠道是否可用
     *
     * @param config 告警配置
     * @return 是否可用
     */
    default boolean isAvailable(AlertConfig config) {
        return true;
    }

    /**
     * 渠道初始化 (可选)
     *
     * @param config 告警配置
     */
    default void initialize(AlertConfig config) {
        // 默认空实现
    }

    /**
     * 渠道销毁 (可选)
     */
    default void destroy() {
        // 默认空实现
    }
}
