package cn.iocoder.yudao.module.flink.monitor.alert.channel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 告警发送结果
 *
 * @author yzh
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertSendResult {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 结果码
     */
    private String resultCode;

    /**
     * 结果消息
     */
    private String resultMsg;

    /**
     * 消息ID (用于追踪)
     */
    private String messageId;

    /**
     * 发送时间
     */
    @Builder.Default
    private LocalDateTime sentTime = LocalDateTime.now();

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 重试次数
     */
    @Builder.Default
    private int retryCount = 0;

    // ==================== 静态工厂方法 ====================

    public static AlertSendResult success() {
        return AlertSendResult.builder()
                .success(true)
                .resultCode("SUCCESS")
                .resultMsg("发送成功")
                .build();
    }

    public static AlertSendResult success(String messageId) {
        return AlertSendResult.builder()
                .success(true)
                .resultCode("SUCCESS")
                .resultMsg("发送成功")
                .messageId(messageId)
                .build();
    }

    public static AlertSendResult failure(String errorMsg) {
        return AlertSendResult.builder()
                .success(false)
                .resultCode("FAILURE")
                .resultMsg("发送失败")
                .errorMsg(errorMsg)
                .build();
    }

    public static AlertSendResult failure(String errorMsg, int retryCount) {
        return AlertSendResult.builder()
                .success(false)
                .resultCode("FAILURE")
                .resultMsg("发送失败")
                .errorMsg(errorMsg)
                .retryCount(retryCount)
                .build();
    }

    // ==================== 实例方法 ====================

    /**
     * 是否可以重试
     */
    public boolean canRetry() {
        return !success && retryCount < 3;
    }

    /**
     * 创建重试结果
     */
    public AlertSendResult createRetryResult() {
        return AlertSendResult.builder()
                .success(false)
                .resultCode(this.resultCode)
                .resultMsg(this.resultMsg)
                .errorMsg(this.errorMsg)
                .retryCount(this.retryCount + 1)
                .sentTime(LocalDateTime.now())
                .build();
    }
}
