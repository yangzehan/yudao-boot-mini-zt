# 实施计划：Flink 任务监控邮件告警功能

## 任务概述

在 Flink 任务监控系统中添加邮件告警功能，使用 Spring Boot 事件监听器模式实现。

## 技术方案

基于项目现有的基础设施，采用最小侵入式设计：
- **事件模式**：Spring ApplicationEvent + @EventListener + @Async
- **邮件集成**：复用现有的 MailSendApi 体系
- **监控集成**：在 UnifiedFlinkMonitor 中检测状态变化并发布事件

## 实施步骤

### 步骤 1: 创建告警事件类

**文件位置**: `yudao-flink-monitor/src/main/java/cn/iocoder/yudao/flink/monitor/event/FlinkJobAlertEvent.java`

```java
package cn.iocoder.yudao.flink.monitor.event;

import lombok.Data;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Flink 任务告警事件
 */
@Data
public class FlinkJobAlertEvent extends ApplicationEvent {

    /** 任务ID */
    private Long jobId;

    /** 任务名称 */
    private String jobName;

    /** 告警类型 */
    private AlertType alertType;

    /** 告警级别 */
    private AlertLevel level;

    /** 告警消息 */
    private String message;

    /** 触发时间 */
    private LocalDateTime triggerTime;

    /** 附加信息 */
    private Map<String, Object> extra;

    public FlinkJobAlertEvent(Object source) {
        super(source);
        this.triggerTime = LocalDateTime.now();
    }

    /**
     * 告警类型枚举
     */
    public enum AlertType {
        JOB_FAILED("任务失败"),
        JOB_RESTART("任务重启"),
        JOB_CANCELLED("任务取消"),
        CHECKPOINT_FAILED("Checkpoint 失败"),
        BACKPRESSURE("背压告警"),
        JOB_SLOW("任务延迟"),
        UNKNOWN("未知异常");

        private final String description;

        AlertType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 告警级别枚举
     */
    public enum AlertLevel {
        INFO("信息"),
        WARNING("警告"),
        CRITICAL("严重");

        private final String description;

        AlertLevel(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
```

### 步骤 2: 创建告警服务

**文件位置**: `yudao-flink-monitor/src/main/java/cn/iocoder/yudao/flink/monitor/service/FlinkJobAlertService.java`

```java
package cn.iocoder.yudao.flink.monitor.service;

import cn.iocoder.yudao.module.infra.api.mail.MailSendApi;
import cn.iocoder.yudao.module.infra.api.mail.dto.MailSendSingleToUserReqDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Flink 任务告警服务
 */
@Slf4j
@Service
public class FlinkJobAlertService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String ALERT_TEMPLATE_CODE = "flink_job_alert";

    @Resource
    private ApplicationContext applicationContext;

    @Resource
    private MailSendApi mailSendApi;

    /**
     * 是否启用告警
     */
    @Value("${flink.alert.enabled:true}")
    private boolean alertEnabled;

    /**
     * 告警接收者邮箱（多个用逗号分隔）
     */
    @Value("${flink.alert.receivers:627617031@qq.com}")
    private String alertReceivers;

    /**
     * 发布告警事件
     */
    public void publishAlert(FlinkJobAlertEvent event) {
        if (!alertEnabled) {
            log.debug("告警功能已禁用，跳过事件发布");
            return;
        }
        applicationContext.publishEvent(event);
        log.info("发布 Flink 任务告警事件: jobId={}, type={}, level={}",
                event.getJobId(), event.getJobName(), event.getAlertType());
    }

    /**
     * 发送告警邮件
     */
    public void sendAlertEmail(FlinkJobAlertEvent event) {
        if (!alertEnabled) {
            log.debug("告警功能已禁用，跳过邮件发送");
            return;
        }

        String[] receivers = alertReceivers.split(",");
        for (String receiver : receivers) {
            try {
                MailSendSingleToUserReqDTO reqDTO = new MailSendSingleToUserReqDTO();
                reqDTO.setMail(receiver.trim());
                reqDTO.setTemplateCode(ALERT_TEMPLATE_CODE);
                reqDTO.setTemplateParams(buildTemplateParams(event));
                reqDTO.setJsonDynamicData(false);

                mailSendApi.sendSingleMailToUser(reqDTO);
                log.info("告警邮件发送成功: jobId={}, receiver={}", event.getJobId(), receiver);
            } catch (Exception e) {
                log.error("告警邮件发送失败: jobId={}, receiver={}", event.getJobId(), receiver, e);
            }
        }
    }

    /**
     * 构建邮件模板参数
     */
    private Map<String, Object> buildTemplateParams(FlinkJobAlertEvent event) {
        Map<String, Object> params = new HashMap<>();
        params.put("jobName", event.getJobName());
        params.put("jobId", event.getJobId());
        params.put("alertType", event.getAlertType().getDescription());
        params.put("level", event.getLevel().getDescription());
        params.put("message", event.getMessage());
        params.put("triggerTime", event.getTriggerTime().format(FORMATTER));

        if (event.getExtra() != null) {
            params.putAll(event.getExtra());
        }

        return params;
    }
}
```

### 步骤 3: 创建告警监听器

**文件位置**: `yudao-flink-monitor/src/main/java/cn/iocoder/yudao/flink/monitor/event/FlinkJobAlertListener.java`

```java
package cn.iocoder.yudao.flink.monitor.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Flink 任务告警事件监听器
 */
@Slf4j
@Component
public class FlinkJobAlertListener {

    @Resource
    private FlinkJobAlertService alertService;

    /**
     * 处理告警事件
     * 根据告警级别决定是否发送邮件通知
     */
    @Async("flinkMonitorExecutor")
    @EventListener
    public void onAlert(FlinkJobAlertEvent event) {
        try {
            log.info("收到告警事件: jobId={}, jobName={}, type={}, level={}",
                    event.getJobId(), event.getJobName(),
                    event.getAlertType(), event.getLevel());

            // 根据告警级别决定通知方式
            switch (event.getLevel()) {
                case CRITICAL:
                    // 严重级别：发送邮件 + 记录告警日志
                    alertService.sendAlertEmail(event);
                    log.error("Flink 任务严重告警: jobId={}, jobName={}, message={}",
                            event.getJobId(), event.getJobName(), event.getMessage());
                    break;

                case WARNING:
                    // 警告级别：发送邮件
                    alertService.sendAlertEmail(event);
                    log.warn("Flink 任务警告告警: jobId={}, jobName={}, message={}",
                            event.getJobId(), event.getJobName(), event.getMessage());
                    break;

                case INFO:
                    // 信息级别：仅记录日志
                    log.info("Flink 任务信息告警: jobId={}, jobName={}, message={}",
                            event.getJobId(), event.getJobName(), event.getMessage());
                    break;

                default:
                    log.info("未知告警级别: jobId={}, level={}",
                            event.getJobId(), event.getLevel());
            }

        } catch (Exception e) {
            log.error("处理告警事件失败: jobId={}", event.getJobId(), e);
        }
    }
}
```

### 步骤 4: 集成到 UnifiedFlinkMonitor（状态变化检测）

**修改文件**: `yudao-flink-monitor/src/main/java/cn/iocoder/yudao/flink/monitor/UnifiedFlinkMonitor.java`

主要修改点：
1. 注入 FlinkJobAlertService
2. 在检测到状态变化时发布告警事件

```java
// 在现有类中添加以下依赖
@Resource
private FlinkJobAlertService alertService;

// 在 handleStatusChange 方法中添加：
private void handleStatusChange(DataJobDto job, JobStatus oldStatus, JobStatus newStatus) {
    // ... 现有逻辑：更新数据库状态

    // 新增：发布告警事件
    publishAlertEvent(job, oldStatus, newStatus);
}

private void publishAlertEvent(DataJobDto job, JobStatus oldStatus, JobStatus newStatus) {
    FlinkJobAlertEvent event = new FlinkJobAlertEvent(this);
    event.setJobId(job.getId());
    event.setJobName(job.getJobName());
    event.setTriggerTime(LocalDateTime.now());

    // 根据状态变化确定告警类型和级别
    switch (newStatus) {
        case FAILED:
            event.setAlertType(FlinkJobAlertEvent.AlertType.JOB_FAILED);
            event.setLevel(FlinkJobAlertEvent.AlertLevel.CRITICAL);
            event.setMessage(String.format("任务状态从 %s 变为 %s，请检查任务日志。",
                    oldStatus.getDescription(), newStatus.getDescription()));
            break;

        case CANCELED:
        case CANCELLING:
            event.setAlertType(FlinkJobAlertEvent.AlertType.JOB_CANCELLED);
            event.setLevel(FlinkJobAlertEvent.AlertLevel.WARNING);
            event.setMessage("任务被取消或正在取消中。");
            break;

        case RESTARTING:
            event.setAlertType(FlinkJobAlertEvent.AlertType.JOB_RESTART);
            event.setLevel(FlinkJobAlertEvent.AlertLevel.WARNING);
            event.setMessage("任务正在重启中。");
            break;

        case FAILING:
            event.setAlertType(FlinkJobAlertEvent.AlertType.JOB_FAILED);
            event.setLevel(FlinkJobAlertEvent.AlertLevel.WARNING);
            event.setMessage("任务正在失败过程中。");
            break;

        default:
            // 其他状态变化不触发告警
            return;
    }

    alertService.publishAlert(event);
}
```

### 步骤 5: 添加配置文件

**修改文件**: `yudao-flink-monitor/src/main/resources/application.yaml`

```yaml
# Flink 告警配置
flink:
  alert:
    enabled: true
    receivers: 627617031@qq.com
    levels:
      critical:
        - JOB_FAILED
      warning:
        - JOB_CANCELLED
        - JOB_RESTART
        - FAILING
      info: []
```

### 步骤 6: 创建邮件模板（数据库初始化）

**SQL 脚本**: 供用户手动执行

```sql
-- 插入 Flink 任务告警邮件模板
INSERT INTO mail_template (
    id, name, code, title, content, status,
    creator, create_time, updater, update_time, deleted
) VALUES (
    1001, 'Flink任务告警模板', 'flink_job_alert',
    '【告警】Flink任务 ${jobName} 发生${alertType}',
    '<html>
<body>
    <h2 style="color: #e74c3c;">Flink 任务告警通知</h2>
    <table style="border-collapse: collapse; width: 100%; max-width: 600px;">
        <tr>
            <td style="padding: 10px; border-bottom: 1px solid #ddd; font-weight: bold;">任务名称</td>
            <td style="padding: 10px; border-bottom: 1px solid #ddd;">${jobName}</td>
        </tr>
        <tr>
            <td style="padding: 10px; border-bottom: 1px solid #ddd; font-weight: bold;">任务ID</td>
            <td style="padding: 10px; border-bottom: 1px solid #ddd;">${jobId}</td>
        </tr>
        <tr>
            <td style="padding: 10px; border-bottom: 1px solid #ddd; font-weight: bold;">告警类型</td>
            <td style="padding: 10px; border-bottom: 1px solid #ddd;">${alertType}</td>
        </tr>
        <tr>
            <td style="padding: 10px; border-bottom: 1px solid #ddd; font-weight: bold;">告警级别</td>
            <td style="padding: 10px; border-bottom: 1px solid #ddd;">
                <span style="color: ${level == '严重' ? '#e74c3c' : '#f39c12'};">${level}</span>
            </td>
        </tr>
        <tr>
            <td style="padding: 10px; border-bottom: 1px solid #ddd; font-weight: bold;">告警消息</td>
            <td style="padding: 10px; border-bottom: 1px solid #ddd;">${message}</td>
        </tr>
        <tr>
            <td style="padding: 10px; border-bottom: 1px solid #ddd; font-weight: bold;">触发时间</td>
            <td style="padding: 10px; border-bottom: 1px solid #ddd;">${triggerTime}</td>
        </tr>
    </table>
    <p style="color: #999; font-size: 12px; margin-top: 20px;">
        此邮件由系统自动发送，请勿回复。
    </p>
</body>
</html>',
    1, 'admin', NOW(), 'admin', NOW(), 0
);
```

### 步骤 7: 确保异步配置正确

**修改文件**（如不存在则创建）: `yudao-flink-monitor/src/main/java/cn/iocoder/yudao/flink/monitor/config/MonitorConfig.java`

```java
package cn.iocoder.yudao.flink.monitor.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 监控模块配置
 */
@Slf4j
@Configuration
@EnableAsync
public class MonitorConfig {

    /**
     * Flink 监控线程池
     */
    @Bean(name = "flinkMonitorExecutor")
    public Executor flinkMonitorExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("flink-monitor-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        log.info("Flink 监控线程池初始化完成");
        return executor;
    }
}
```

### 步骤 8: 单元测试

**文件位置**: `yudao-flink-monitor/src/test/java/cn/iocoder/yudao/flink/monitor/FlinkJobAlertTest.java`

```java
package cn.iocoder.yudao.flink.monitor;

import cn.iocoder.yudao.flink.monitor.event.FlinkJobAlertEvent;
import cn.iocoder.yudao.flink.monitor.service.FlinkJobAlertService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("local")
class FlinkJobAlertTest {

    @Autowired
    private FlinkJobAlertService alertService;

    @Test
    void testPublishAlertEvent() {
        FlinkJobAlertEvent event = new FlinkJobAlertEvent(this);
        event.setJobId(1L);
        event.setJobName("test-flink-job");
        event.setAlertType(FlinkJobAlertEvent.AlertType.JOB_FAILED);
        event.setLevel(FlinkJobAlertEvent.AlertLevel.CRITICAL);
        event.setMessage("测试告警消息");

        alertService.publishAlert(event);

        // 等待异步处理完成
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
```

### 步骤 9: 集成测试（发送测试邮件）

创建一个测试 Controller 便于手动测试：

```java
@RestController
@RequestMapping("/admin-api/flink/alert/test")
public class FlinkAlertTestController {

    @Resource
    private FlinkJobAlertService alertService;

    @GetMapping("/send-test-email")
    public CommonResult<String> sendTestEmail() {
        FlinkJobAlertEvent event = new FlinkJobAlertEvent(this);
        event.setJobId(999L);
        event.setJobName("测试Flink任务");
        event.setAlertType(FlinkJobAlertEvent.AlertType.JOB_FAILED);
        event.setLevel(FlinkJobAlertEvent.AlertLevel.CRITICAL);
        event.setMessage("这是一封测试邮件，用于验证 Flink 告警邮件功能是否正常。");
        event.setTriggerTime(LocalDateTime.now());

        alertService.sendAlertEmail(event);
        return CommonResult.success("测试邮件已发送，请检查收件箱: 627617031@qq.com");
    }
}
```

## 关键文件清单

| 文件 | 操作 | 说明 |
|------|------|------|
| `yudao-flink-monitor/.../event/FlinkJobAlertEvent.java` | 新建 | 告警事件类 |
| `yudao-flink-monitor/.../service/FlinkJobAlertService.java` | 新建 | 告警服务（核心逻辑） |
| `yudao-flink-monitor/.../event/FlinkJobAlertListener.java` | 新建 | 告警事件监听器 |
| `yudao-flink-monitor/.../UnifiedFlinkMonitor.java` | 修改 | 集成状态变化检测和事件发布 |
| `yudao-flink-monitor/.../config/MonitorConfig.java` | 新建 | 异步线程池配置 |
| `yudao-flink-monitor/.../resources/application.yaml` | 修改 | 添加告警配置 |
| 数据库 | 修改 | 插入邮件模板记录 |

## 架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                    UnifiedFlinkMonitor                          │
│  (定时轮询 5秒)                                                  │
└─────────────────────────┬───────────────────────────────────────┘
                          │
              ┌───────────▼───────────┐
              │  状态变化检测           │
              └───────────┬───────────┘
                          │
              ┌───────────▼───────────┐
              │  FlinkJobAlertEvent   │
              │  (Spring Event)       │
              └───────────┬───────────┘
                          │
              ┌───────────▼───────────┐
              │ FlinkJobAlertListener │
              │   (@Async + @Event)   │
              └───────────┬───────────┘
                          │
              ┌───────────▼───────────┐
              │ FlinkJobAlertService  │
              │   - publishAlert()    │
              │   - sendAlertEmail()  │
              └───────────┬───────────┘
                          │
              ┌───────────▼───────────┐
              │     MailSendApi      │
              │  (现有邮件服务体系)     │
              └───────────────────────┘
                          │
              ┌───────────▼───────────┐
              │  MailSendConsumer     │
              │  (@Async + @Event)   │
              └───────────┬───────────┘
                          │
              ┌───────────▼───────────┐
              │    MailUtil.send()   │
              │     (Hutool)          │
              └───────────────────────┘
```

## 风险与缓解措施

| 风险 | 可能性 | 影响 | 缓解措施 |
|------|--------|------|----------|
| 事件积压导致内存溢出 | 低 | 高 | 使用有界队列 ThreadPoolTaskExecutor |
| 邮件发送失败 | 中 | 低 | MailSendConsumer 已有重试机制 |
| 循环依赖 | 低 | 中 | AlertService 依赖 MailSendApi 接口 |
| 告警风暴（同一任务频繁告警） | 中 | 中 | 可添加告警冷却期配置 |
| 邮箱配置泄露 | 低 | 高 | 使用 application-local.yaml 管理 |

## 测试要点

1. **单元测试**：测试事件发布、监听器处理
2. **集成测试**：测试邮件发送完整链路
3. **手动测试**：调用测试接口发送测试邮件到 627617031@qq.com

## 后续扩展（可选）

1. **告警规则配置化**：支持按任务配置不同的告警规则
2. **告警抑制**：相同告警 n 分钟内不重复发送
3. **多渠道通知**：支持短信、钉钉、企业微信
4. **告警历史**：记录告警发送历史便于查询
