package cn.iocoder.yudao.module.flink.monitor.alert.dedup;

import cn.iocoder.yudao.module.flink.monitor.alert.model.AlertConfig;
import cn.iocoder.yudao.module.flink.monitor.alert.model.AlertMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 告警去重服务
 *
 * <p>实现作业级别的告警去重策略:</p>
 * <ul>
 *   <li>同一作业、同一类型、同一级别的告警在去重窗口内不重复发送</li>
 *   <li>支持配置最大告警次数</li>
 *   <li>支持基于 Redis 的分布式去重</li>
 * </ul>
 *
 * @author yzh
 */
@Slf4j
@Service
public class AlertDedupService {

    /**
     * 去重键前缀
     */
    private static final String DEDUP_KEY_PREFIX = "flink:alert:dedup:";

    /**
     * 计数键前缀
     */
    private static final String COUNT_KEY_PREFIX = "flink:alert:count:";

    /**
     * 默认去重窗口时间（秒）
     */
    private static final int DEFAULT_WINDOW_SECONDS = 1800;

    /**
     * 默认最大告警次数
     */
    private static final int DEFAULT_MAX_COUNT = 3;

    @Resource
    private RedisTemplate<Object, Object> redisTemplate;

    /**
     * 检查告警是否应该被发送
     *
     * @param message 告警消息
     * @param config  告警配置
     * @return 去重检查结果
     */
    public DedupCheckResult check(AlertMessage message, AlertConfig config) {
        // 空消息检查
        if (message == null) {
            log.warn("收到空告警消息，允许发送");
            return DedupCheckResult.allow();
        }

        // 空配置检查
        if (config == null || config.getDedup() == null) {
            return DedupCheckResult.allow();
        }

        AlertConfig.DedupConfig dedupConfig = config.getDedup();
        if (!Boolean.TRUE.equals(dedupConfig.getEnabled())) {
            return DedupCheckResult.allow();
        }

        String dedupKey = buildDedupKey(message);

        // 处理空值，使用默认值
        Integer windowSeconds = dedupConfig.getWindowSeconds();
        if (windowSeconds == null || windowSeconds <= 0) {
            windowSeconds = DEFAULT_WINDOW_SECONDS;
        }

        Integer maxCount = dedupConfig.getMaxCount();
        if (maxCount == null || maxCount <= 0) {
            maxCount = DEFAULT_MAX_COUNT;
        }

        try {
            // 检查是否在去重窗口内
            Object existing = redisTemplate.opsForValue().get(dedupKey);
            if (existing != null) {
                // 获取当前计数
                String countKey = buildCountKey(message);
                Object countObj = redisTemplate.opsForValue().get(countKey);
                int currentCount = parseCountSafely(countObj);

                if (currentCount >= maxCount) {
                    log.debug("告警去重: 超过最大次数, key={}, count={}", dedupKey, currentCount);
                    return DedupCheckResult.deny(
                            String.format("超过最大告警次数(%d)", maxCount),
                            currentCount
                    );
                }

                // 增加计数
                redisTemplate.opsForValue().increment(countKey);
                log.debug("告警去重: 在去重窗口内, 增加计数, key={}, count={}", dedupKey, currentCount + 1);

                // 更新去重键的TTL
                Long ttl = redisTemplate.getExpire(dedupKey, TimeUnit.SECONDS);
                if (ttl != null && ttl > 0) {
                    redisTemplate.expire(dedupKey, Duration.ofSeconds(ttl));
                }

                return DedupCheckResult.allow();
            }

            // 首次触发，设置去重键
            redisTemplate.opsForValue().set(
                    dedupKey,
                    message.getAlertId(),
                    Duration.ofSeconds(windowSeconds)
            );

            // 设置计数键
            String countKey = buildCountKey(message);
            redisTemplate.opsForValue().set(
                    countKey,
                    1,
                    Duration.ofSeconds(windowSeconds)
            );

            log.debug("告警去重: 首次触发, 设置去重窗口, key={}, window={}s", dedupKey, windowSeconds);
            return DedupCheckResult.allow();

        } catch (Exception e) {
            log.error("告警去重检查失败: key={}", dedupKey, e);
            // 去重检查失败时允许发送，避免阻塞正常告警
            return DedupCheckResult.allow();
        }
    }

    /**
     * 清除指定告警的去重状态 (用于恢复通知或手动清除)
     */
    public void clear(AlertMessage message) {
        if (message == null) {
            log.warn("尝试清除空消息的去重状态，已忽略");
            return;
        }

        String dedupKey = buildDedupKey(message);
        String countKey = buildCountKey(message);

        try {
            redisTemplate.delete(dedupKey);
            redisTemplate.delete(countKey);
            log.debug("清除告警去重状态: jobId={}", message.getJobId());
        } catch (Exception e) {
            log.error("清除告警去重状态失败: jobId={}", message.getJobId(), e);
        }
    }

    /**
     * 获取去重状态
     */
    public DedupStatus getStatus(AlertMessage message) {
        if (message == null) {
            return DedupStatus.builder()
                    .dedupKey("unknown")
                    .isInWindow(false)
                    .triggerCount(0)
                    .remainingSeconds(0)
                    .lastAlertTime(null)
                    .build();
        }

        String dedupKey = buildDedupKey(message);
        String countKey = buildCountKey(message);

        try {
            Object dedupValue = redisTemplate.opsForValue().get(dedupKey);
            Object countValue = redisTemplate.opsForValue().get(countKey);

            Long ttl = redisTemplate.getExpire(dedupKey, TimeUnit.SECONDS);

            return DedupStatus.builder()
                    .dedupKey(dedupKey)
                    .isInWindow(dedupValue != null)
                    .triggerCount(parseCountSafely(countValue))
                    .remainingSeconds(ttl != null && ttl > 0 ? ttl : 0)
                    .lastAlertTime(dedupValue != null ? LocalDateTime.now() : null)
                    .build();
        } catch (Exception e) {
            log.error("获取去重状态失败: key={}", dedupKey, e);
            return DedupStatus.builder()
                    .dedupKey(dedupKey)
                    .isInWindow(false)
                    .triggerCount(0)
                    .remainingSeconds(0)
                    .lastAlertTime(null)
                    .build();
        }
    }

    /**
     * 安全地解析计数值
     */
    private int parseCountSafely(Object countObj) {
        if (countObj == null) {
            return 1;
        }
        try {
            return Integer.parseInt(countObj.toString());
        } catch (NumberFormatException e) {
            log.warn("无法解析计数值，使用默认值1: countObj={}", countObj);
            return 1;
        }
    }

    /**
     * 构建去重键（包含作业ID、类型和级别）
     */
    private String buildDedupKey(AlertMessage message) {
        String typeName = message.getType() != null ? message.getType().name() : "UNKNOWN";
        String levelName = message.getLevel() != null ? message.getLevel().name() : "UNKNOWN";
        return DEDUP_KEY_PREFIX + message.getJobId() + ":" + typeName + ":" + levelName;
    }

    /**
     * 构建计数键（包含作业ID、类型和级别）
     */
    private String buildCountKey(AlertMessage message) {
        String typeName = message.getType() != null ? message.getType().name() : "UNKNOWN";
        String levelName = message.getLevel() != null ? message.getLevel().name() : "UNKNOWN";
        return COUNT_KEY_PREFIX + message.getJobId() + ":" + typeName + ":" + levelName;
    }

    // ==================== 内部类 ====================

    public static class DedupCheckResult {

        private final boolean allowed;
        private final String reason;
        private final int currentCount;

        private DedupCheckResult(boolean allowed, String reason, int currentCount) {
            this.allowed = allowed;
            this.reason = reason;
            this.currentCount = currentCount;
        }

        public boolean isAllowed() {
            return allowed;
        }

        public String getReason() {
            return reason;
        }

        public int getCurrentCount() {
            return currentCount;
        }

        public static DedupCheckResult allow() {
            return new DedupCheckResult(true, null, 0);
        }

        public static DedupCheckResult deny(String reason, int currentCount) {
            return new DedupCheckResult(false, reason, currentCount);
        }
    }

    public static class DedupStatus {

        private final String dedupKey;
        private final boolean isInWindow;
        private final int triggerCount;
        private final long remainingSeconds;
        private final LocalDateTime lastAlertTime;

        private DedupStatus(String dedupKey, boolean isInWindow, int triggerCount,
                           long remainingSeconds, LocalDateTime lastAlertTime) {
            this.dedupKey = dedupKey;
            this.isInWindow = isInWindow;
            this.triggerCount = triggerCount;
            this.remainingSeconds = remainingSeconds;
            this.lastAlertTime = lastAlertTime;
        }

        public String getDedupKey() {
            return dedupKey;
        }

        public boolean isInWindow() {
            return isInWindow;
        }

        public int getTriggerCount() {
            return triggerCount;
        }

        public long getRemainingSeconds() {
            return remainingSeconds;
        }

        public LocalDateTime getLastAlertTime() {
            return lastAlertTime;
        }

        public static DedupStatusBuilder builder() {
            return new DedupStatusBuilder();
        }
    }

    public static class DedupStatusBuilder {
        private String dedupKey;
        private boolean isInWindow;
        private int triggerCount;
        private long remainingSeconds;
        private LocalDateTime lastAlertTime;

        public DedupStatusBuilder dedupKey(String dedupKey) {
            this.dedupKey = dedupKey;
            return this;
        }

        public DedupStatusBuilder isInWindow(boolean isInWindow) {
            this.isInWindow = isInWindow;
            return this;
        }

        public DedupStatusBuilder triggerCount(int triggerCount) {
            this.triggerCount = triggerCount;
            return this;
        }

        public DedupStatusBuilder remainingSeconds(long remainingSeconds) {
            this.remainingSeconds = remainingSeconds;
            return this;
        }

        public DedupStatusBuilder lastAlertTime(LocalDateTime lastAlertTime) {
            this.lastAlertTime = lastAlertTime;
            return this;
        }

        public DedupStatus build() {
            return new DedupStatus(dedupKey, isInWindow, triggerCount, remainingSeconds, lastAlertTime);
        }
    }
}
