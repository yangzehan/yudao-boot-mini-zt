package cn.iocoder.yudao.module.flink.monitor.alert.event;

import cn.iocoder.yudao.module.datastudio.dal.dataobject.flinkcluster.FlinkClusterDO;
import cn.iocoder.yudao.module.datastudio.dal.mysql.flinkcluster.FlinkClusterMapper;
import cn.iocoder.yudao.module.flink.monitor.alert.AlertCoordinator;
import cn.iocoder.yudao.module.flink.monitor.alert.model.AlertConfig;
import cn.iocoder.yudao.module.flink.monitor.alert.model.AlertMessage;
import cn.iocoder.yudao.module.flink.monitor.alert.service.AlertReceiverService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 告警事件监听器
 *
 * <p>异步处理告警事件，执行告警协调逻辑</p>
 *
 * @author yzh
 */
@Slf4j
@Component
public class AlertEventListener {

    /**
     * 告警线程池名称
     */
    private static final String EXECUTOR_NAME = "flinkMonitorExecutor";

    @Resource
    private AlertCoordinator alertCoordinator;

    @Resource
    private AlertReceiverService receiverService;

    @Resource
    private FlinkClusterMapper flinkClusterMapper;

    /**
     * 处理告警事件
     *
     * @param event 告警事件
     */
    @Async(EXECUTOR_NAME)
    @EventListener
    public void onAlertEvent(FlinkJobAlertEvent event) {
        try {
            AlertMessage message = event.getMessage();

            // 空消息检查
            if (message == null) {
                log.warn("收到空告警消息，已忽略");
                return;
            }

            log.info("收到告警事件: jobId={}, jobName={}, type={}, level={}, eventType={}",
                    message.getJobId(), message.getJobName(),
                    message.getType(), message.getLevel(), event.getEventType());

            if (FlinkJobAlertEvent.EventType.RECOVERY.equals(event.getEventType())) {
                // 处理恢复通知
                handleRecoveryEvent(event);
            } else {
                // 处理告警事件
                handleAlertEvent(event);
            }

        } catch (Exception e) {
            log.error("处理告警事件失败: event={}", event, e);
        }
    }

    /**
     * 处理告警事件
     */
    private void handleAlertEvent(FlinkJobAlertEvent event) {
        AlertMessage message = event.getMessage();

        // 1. 获取告警配置
        AlertConfig config = getAlertConfig(message);

        // 2. 获取接收人信息
        AlertReceiverService.ReceiverInfo receiverInfo =
                receiverService.getReceivers(message.getJobId(), config);

        // 3. 设置接收人信息到消息
        if (!receiverInfo.isEmpty()) {
            message.setReceiverIds(receiverInfo.getUserIdList());
            message.setReceiverEmails(receiverInfo.getEmailList());
        }

        // 4. 执行告警协调
        AlertCoordinator.AlertHandleResult result = alertCoordinator.handle(message, config);

        log.info("告警处理结果: jobId={}, success={}, allowed={}, reason={}",
                message.getJobId(), result.isSuccess(), result.isAllowed(), result.getMessage());
    }

    /**
     * 处理恢复通知事件
     */
    private void handleRecoveryEvent(FlinkJobAlertEvent event) {
        AlertMessage message = event.getMessage();

        // 获取告警配置
        AlertConfig config = getAlertConfig(message);

        // 执行恢复通知协调
        AlertCoordinator.AlertHandleResult result = alertCoordinator.handleRecovery(message, config);

        log.info("恢复通知处理结果: jobId={}, success={}",
                message.getJobId(), result.isSuccess());
    }

    /**
     * 获取告警配置
     *
     * <p>从 FlinkClusterDO.alert_config 字段获取配置</p>
     * <p>如果没有配置，返回默认配置</p>
     */
    private AlertConfig getAlertConfig(AlertMessage message) {
        try {
            // 如果有集群ID，从集群配置中获取
            if (message.getClusterId() != null) {
                FlinkClusterDO cluster = flinkClusterMapper.selectById(message.getClusterId());
                if (cluster != null && cluster.getAlertConfig() != null) {
                    String alertConfigJson = cluster.getAlertConfig();
                    return AlertConfig.parse(alertConfigJson);
                }
            }

            // 返回默认配置
            return AlertConfig.getDefaultConfig();
        } catch (Exception e) {
            log.error("获取告警配置失败: clusterId={}", message.getClusterId(), e);
            return AlertConfig.getDefaultConfig();
        }
    }
}
