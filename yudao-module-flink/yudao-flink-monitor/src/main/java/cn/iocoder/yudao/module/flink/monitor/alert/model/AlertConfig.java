package cn.iocoder.yudao.module.flink.monitor.alert.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 告警配置模型
 *
 * <p>对应 FlinkClusterDO.alert_config JSON 字段</p>
 *
 * <p>配置示例:</p>
 * <pre>
 * {
 *   "enabled": true,
 *   "channels": ["mail"],
 *   "rules": [
 *     {
 *       "type": "JOB_FAILED",
 *       "level": "CRITICAL",
 *       "enabled": true,
 *       "channels": ["mail"],
 *       "cooldownSeconds": 300
 *     }
 *   ],
 *   "receivers": {
 *     "userIds": [1, 2, 3],
 *     "roles": ["ADMIN"]
 *   },
 *   "dedup": {
 *     "enabled": true,
 *     "windowSeconds": 1800,
 *     "maxCount": 3
 *   }
 * }
 * </pre>
 *
 * @author yzh
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AlertConfig {

    /**
     * 是否启用告警
     */
    @Builder.Default
    private Boolean enabled = true;

    /**
     * 启用的告警渠道列表
     */
    private List<String> channels;

    /**
     * 告警规则列表
     */
    private List<AlertRule> rules;

    /**
     * 告警接收人配置
     */
    private ReceiverConfig receivers;

    /**
     * 去重配置
     */
    private DedupConfig dedup;

    /**
     * 静默配置 (告警抑制)
     */
    private SilenceConfig silence;

    // ==================== 内部类 ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlertRule {

        /**
         * 告警类型
         */
        private String type;

        /**
         * 告警级别
         */
        private String level;

        /**
         * 是否启用
         */
        @Builder.Default
        private Boolean enabled = true;

        /**
         * 渠道列表 (覆盖全局配置)
         */
        private List<String> channels;

        /**
         * 冷却时间(秒) - 避免重复告警
         */
        @Builder.Default
        private Integer cooldownSeconds = 300;

        /**
         * 是否发送恢复通知
         */
        @Builder.Default
        private Boolean sendRecovery = true;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReceiverConfig {

        /**
         * 用户ID列表
         */
        private List<Long> userIds;

        /**
         * 角色列表
         */
        private List<String> roles;

        /**
         * 额外邮箱列表
         */
        private List<String> extraEmails;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DedupConfig {

        /**
         * 是否启用去重
         */
        @Builder.Default
        private Boolean enabled = true;

        /**
         * 去重窗口时间(秒)
         */
        @Builder.Default
        private Integer windowSeconds = 1800;

        /**
         * 最大告警次数
         */
        @Builder.Default
        private Integer maxCount = 3;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SilenceConfig {

        /**
         * 是否启用静默期
         */
        @Builder.Default
        private Boolean enabled = false;

        /**
         * 静默开始时间 (HH:mm格式)
         */
        private String startTime;

        /**
         * 静默结束时间 (HH:mm格式)
         */
        private String endTime;

        /**
         * 静默的告警级别
         */
        private List<String> levels;
    }

    // ==================== 静态工厂方法 ====================

    /**
     * 从 JSON 字符串解析配置
     */
    public static AlertConfig parse(String json) {
        if (json == null || json.isEmpty()) {
            return getDefaultConfig();
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper =
                    new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(json, AlertConfig.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("解析告警配置失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取默认配置
     */
    public static AlertConfig getDefaultConfig() {
        return AlertConfig.builder()
                .enabled(true)
                .channels(java.util.Arrays.asList("mail"))
                .dedup(DedupConfig.builder()
                        .enabled(true)
                        .windowSeconds(1800)
                        .maxCount(3)
                        .build())
                .build();
    }

    /**
     * 获取指定告警类型的规则
     */
    public AlertRule getRuleByType(String type) {
        if (rules == null) {
            return null;
        }
        return rules.stream()
                .filter(r -> r.getType().equalsIgnoreCase(type))
                .findFirst()
                .orElse(null);
    }

    /**
     * 检查指定渠道是否启用
     */
    public boolean isChannelEnabled(String channel) {
        if (channels == null) {
            return false;
        }
        return channels.stream()
                .anyMatch(c -> c.equalsIgnoreCase(channel));
    }
}
