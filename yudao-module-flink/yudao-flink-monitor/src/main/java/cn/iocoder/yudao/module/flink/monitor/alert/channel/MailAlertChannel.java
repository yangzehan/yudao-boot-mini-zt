package cn.iocoder.yudao.module.flink.monitor.alert.channel;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.flink.monitor.alert.model.AlertConfig;
import cn.iocoder.yudao.module.flink.monitor.alert.model.AlertMessage;
import cn.iocoder.yudao.module.system.api.mail.MailSendApi;
import cn.iocoder.yudao.module.system.api.mail.dto.MailSendSingleToUserReqDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 邮件告警渠道实现
 *
 * <p>使用现有的 MailSendApi 发送告警邮件</p>
 *
 * @author yzh
 */
@Slf4j
@Component
public class MailAlertChannel implements AlertChannel {

    private static final String CHANNEL_ID = "mail";
    private static final String CHANNEL_NAME = "邮件告警";
    private static final int PRIORITY = 10;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 作业失败告警模板编码
     */
    public static final String TEMPLATE_CODE_JOB_FAILED = "flink_job_failed_alert";

    /**
     * 作业恢复通知模板编码
     */
    public static final String TEMPLATE_CODE_JOB_RECOVERY = "flink_job_recovery_alert";

    @Resource
    private MailSendApi mailSendApi;

    @Override
    public String getChannelId() {
        return CHANNEL_ID;
    }

    @Override
    public String getChannelName() {
        return CHANNEL_NAME;
    }

    @Override
    public int getPriority() {
        return PRIORITY;
    }

    @Override
    public AlertSendResult send(AlertMessage message, AlertConfig config) {
        log.info("开始发送邮件告警: jobId={}, jobName={}", message.getJobId(), message.getJobName());

        // 检查邮箱列表
        List<String> receivers = message.getReceiverEmails();
        if (CollUtil.isEmpty(receivers)) {
            log.warn("没有告警接收人邮箱，跳过邮件发送: jobId={}", message.getJobId());
            return AlertSendResult.failure("没有告警接收人邮箱");
        }

        try {
            // 构建邮件模板参数
            Map<String, Object> templateParams = buildTemplateParams(message);

            // 逐个发送邮件
            int successCount = 0;
            int failCount = 0;
            String lastError = null;

            for (String receiver : receivers) {
                try {
                    MailSendSingleToUserReqDTO reqDTO = new MailSendSingleToUserReqDTO();
                    reqDTO.setToMails(CollUtil.newArrayList(receiver));
                    reqDTO.setTemplateCode(TEMPLATE_CODE_JOB_FAILED);
                    reqDTO.setTemplateParams(templateParams);

                    Long logId = mailSendApi.sendSingleMailToAdmin(reqDTO);
                    log.info("邮件发送成功: jobId={}, receiver={}, mailLogId={}",
                            message.getJobId(), receiver, logId);
                    successCount++;
                } catch (Exception e) {
                    log.error("邮件发送失败: jobId={}, receiver={}", message.getJobId(), receiver, e);
                    lastError = e.getMessage();
                    failCount++;
                }
            }

            // 返回结果
            if (failCount == 0) {
                return AlertSendResult.success();
            } else if (successCount > 0) {
                // 部分成功
                return AlertSendResult.builder()
                        .success(true)
                        .resultCode("PARTIAL_SUCCESS")
                        .resultMsg(String.format("成功 %d 封, 失败 %d 封", successCount, failCount))
                        .errorMsg(lastError)
                        .build();
            } else {
                return AlertSendResult.failure("所有邮件发送失败: " + lastError);
            }

        } catch (Exception e) {
            log.error("邮件告警处理异常: jobId={}", message.getJobId(), e);
            return AlertSendResult.failure("邮件告警处理异常: " + e.getMessage());
        }
    }

    @Override
    public AlertSendResult sendRecovery(AlertMessage message, AlertConfig config) {
        log.info("发送恢复通知邮件: jobId={}, jobName={}", message.getJobId(), message.getJobName());

        List<String> receivers = message.getReceiverEmails();
        if (CollUtil.isEmpty(receivers)) {
            return AlertSendResult.failure("没有告警接收人邮箱");
        }

        try {
            Map<String, Object> templateParams = buildRecoveryTemplateParams(message);

            int successCount = 0;
            int failCount = 0;
            String lastError = null;

            for (String receiver : receivers) {
                try {
                    MailSendSingleToUserReqDTO reqDTO = new MailSendSingleToUserReqDTO();
                    reqDTO.setToMails(CollUtil.newArrayList(receiver));
                    reqDTO.setTemplateCode(TEMPLATE_CODE_JOB_RECOVERY);
                    reqDTO.setTemplateParams(templateParams);

                    mailSendApi.sendSingleMailToAdmin(reqDTO);
                    successCount++;
                } catch (Exception e) {
                    log.error("恢复通知邮件发送失败: jobId={}, receiver={}", message.getJobId(), receiver, e);
                    lastError = e.getMessage();
                    failCount++;
                }
            }

            if (failCount == 0) {
                return AlertSendResult.success();
            } else {
                return AlertSendResult.builder()
                        .success(successCount > 0)
                        .resultCode(successCount > 0 ? "PARTIAL_SUCCESS" : "FAILURE")
                        .resultMsg(String.format("成功 %d 封, 失败 %d 封", successCount, failCount))
                        .errorMsg(lastError)
                        .build();
            }

        } catch (Exception e) {
            log.error("恢复通知邮件处理异常: jobId={}", message.getJobId(), e);
            return AlertSendResult.failure("恢复通知邮件处理异常: " + e.getMessage());
        }
    }

    @Override
    public boolean isAvailable(AlertConfig config) {
        // 检查邮件渠道是否在配置中启用
        return config.isChannelEnabled(CHANNEL_ID);
    }

    // ==================== 私有方法 ====================

    /**
     * 构建邮件模板参数
     */
    private Map<String, Object> buildTemplateParams(AlertMessage message) {
        Map<String, Object> params = new HashMap<>();

        // 基础参数
        params.put("jobName", message.getJobName());
        params.put("jobId", message.getJobId());
        params.put("errorMsg", message.getErrorMsg() != null ? message.getErrorMsg() : "未知错误");
        params.put("occurredTime", message.getOccurredTime() != null
                ? message.getOccurredTime().format(FORMATTER)
                : java.time.LocalDateTime.now().format(FORMATTER));

        // 扩展参数
        if (message.getExtendParams() != null) {
            params.putAll(message.getExtendParams());
        }

        return params;
    }

    /**
     * 构建恢复通知模板参数
     */
    private Map<String, Object> buildRecoveryTemplateParams(AlertMessage message) {
        Map<String, Object> params = buildTemplateParams(message);
        params.put("recoveredTime", java.time.LocalDateTime.now().format(FORMATTER));
        params.put("alertCount", message.getTriggerCount());
        return params;
    }
}
