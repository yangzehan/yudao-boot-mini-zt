package cn.iocoder.yudao.module.flink.monitor.alert.event;

import cn.iocoder.yudao.module.flink.monitor.alert.model.AlertMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 告警事件发布器
 *
 * <p>使用 Spring ApplicationEvent 机制发布告警事件</p>
 * <p>支持异步处理，避免阻塞监控线程</p>
 *
 * @author yzh
 */
@Slf4j
@Component
public class AlertEventPublisher {

    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    /**
     * 发布告警事件
     *
     * @param message 告警消息
     */
    public void publish(AlertMessage message) {
        if (message == null) {
            log.warn("尝试发布空告警消息，已忽略");
            return;
        }

        FlinkJobAlertEvent event = new FlinkJobAlertEvent(message);
        applicationEventPublisher.publishEvent(event);

        log.info("告警事件已发布: jobId={}, type={}, level={}",
                message.getJobId(), message.getType(), message.getLevel());
    }

    /**
     * 发布恢复通知事件
     *
     * @param message 原始告警消息
     */
    public void publishRecovery(AlertMessage message) {
        if (message == null) {
            return;
        }

        message.setRecovered(true);
        FlinkJobAlertEvent event = new FlinkJobAlertEvent(message);
        event.setEventType(FlinkJobAlertEvent.EventType.RECOVERY);
        applicationEventPublisher.publishEvent(event);

        log.info("恢复通知事件已发布: jobId={}", message.getJobId());
    }
}
