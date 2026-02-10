package cn.iocoder.yudao.module.flink.monitor.alert.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Flink 作业告警消息模型
 *
 * <p>封装告警的所有信息，支持在责任链中传递和转换</p>
 *
 * @author yzh
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertMessage {

    /**
     * 告警唯一标识 (用于去重)
     */
    private String alertId;

    /**
     * 告警类型
     */
    private AlertType type;

    /**
     * 告警级别
     */
    private AlertLevel level;

    /**
     * 作业ID (Flink Job ID)
     */
    private String jobId;

    /**
     * 作业名称
     */
    private String jobName;

    /**
     * 集群ID
     */
    private Long clusterId;

    /**
     * 告警标题
     */
    private String title;

    /**
     * 告警内容
     */
    private String content;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 发生时间
     */
    private LocalDateTime occurredTime;

    /**
     * 告警接收人ID列表
     */
    private List<Long> receiverIds;

    /**
     * 告警接收邮箱列表 (用于邮件渠道)
     */
    private List<String> receiverEmails;

    /**
     * 扩展参数 (用于模板渲染)
     */
    private Map<String, Object> extendParams;

    /**
     * 来源监控器标识
     */
    private String sourceMonitor;

    /**
     * 告警配置ID
     */
    private Long alertConfigId;

    /**
     * 是否已恢复
     */
    private boolean recovered;

    /**
     * 告警触发次数
     */
    private int triggerCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    // ==================== 内部方法 ====================

    /**
     * 生成去重键
     */
    public String getDedupKey() {
        return String.format("%s:%s:%s", jobId, type.name(), level.name());
    }

    /**
     * 检查是否为严重告警
     */
    public boolean isCritical() {
        return AlertLevel.CRITICAL.equals(level);
    }

    /**
     * 添加扩展参数
     */
    public AlertMessage addExtendParam(String key, Object value) {
        if (this.extendParams == null) {
            this.extendParams = new HashMap<>();
        }
        this.extendParams.put(key, value);
        return this;
    }

    /**
     * 静态工厂方法 - 创建作业失败告警
     */
    public static AlertMessage createJobFailedAlert(String jobId, String jobName,
                                                     String errorMsg, LocalDateTime occurredTime) {
        return AlertMessage.builder()
                .alertId(java.util.UUID.randomUUID().toString())
                .type(AlertType.JOB_FAILED)
                .level(AlertLevel.CRITICAL)
                .jobId(jobId)
                .jobName(jobName)
                .errorMsg(errorMsg)
                .occurredTime(occurredTime)
                .title(String.format("[CRITICAL] Flink作业失败告警 - %s", jobName))
                .content(buildJobFailedContent(jobId, jobName, errorMsg, occurredTime))
                .createTime(LocalDateTime.now())
                .triggerCount(1)
                .recovered(false)
                .build();
    }

    private static String buildJobFailedContent(String jobId, String jobName,
                                                 String errorMsg, LocalDateTime occurredTime) {
        return String.format(
                "Flink作业 [%s] 在 %s 发生失败。\n错误信息: %s",
                jobName, occurredTime, errorMsg != null ? errorMsg : "未知错误"
        );
    }
}
