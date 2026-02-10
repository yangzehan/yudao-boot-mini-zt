package cn.iocoder.yudao.module.flink.monitor.alert.event;

import cn.iocoder.yudao.module.flink.monitor.alert.model.AlertMessage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * Flink 作业告警事件
 *
 * <p>Spring ApplicationEvent 实现，支持异步监听</p>
 *
 * @author yzh
 */
@Slf4j
@Getter
public class FlinkJobAlertEvent extends ApplicationEvent {

    /**
     * 告警消息
     */
    private final AlertMessage message;

    /**
     * 事件类型
     */
    private EventType eventType;

    /**
     * 触发时间
     */
    private final LocalDateTime triggerTime;

    public FlinkJobAlertEvent(AlertMessage message) {
        super(message);
        this.message = message;
        this.eventType = EventType.ALERT;
        this.triggerTime = LocalDateTime.now();
    }

    public FlinkJobAlertEvent(AlertMessage message, EventType eventType) {
        super(message);
        this.message = message;
        this.eventType = eventType;
        this.triggerTime = LocalDateTime.now();
    }

    /**
     * 事件类型
     */
    public enum EventType {
        /**
         * 告警事件
         */
        ALERT,

        /**
         * 恢复通知事件
         */
        RECOVERY
    }

    // ==================== 便捷方法 ====================

    public String getJobId() {
        return message.getJobId();
    }

    public String getJobName() {
        return message.getJobName();
    }

    public String getAlertType() {
        return message.getType().name();
    }

    public String getAlertLevel() {
        return message.getLevel().name();
    }

    public boolean isCritical() {
        return message.isCritical();
    }

    public boolean isRecovery() {
        return EventType.RECOVERY.equals(eventType);
    }
}
