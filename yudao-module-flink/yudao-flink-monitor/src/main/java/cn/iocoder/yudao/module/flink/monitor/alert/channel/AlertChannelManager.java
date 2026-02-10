package cn.iocoder.yudao.module.flink.monitor.alert.channel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 告警渠道管理器
 *
 * <p>负责管理所有告警渠道的注册、发现和调用</p>
 * <p>支持 SPI 机制自动加载所有实现类</p>
 *
 * @author yzh
 */
@Slf4j
@Component
public class AlertChannelManager {

    /**
     * 渠道缓存 (channelId -> AlertChannel)
     */
    private final Map<String, AlertChannel> channelCache = new ConcurrentHashMap<>();

    @Resource
    private List<AlertChannel> alertChannels;

    /**
     * 初始化 - 注册所有告警渠道
     */
    @PostConstruct
    public void init() {
        log.info("开始初始化告警渠道管理器");

        // 注册所有 Spring 注入的渠道
        if (alertChannels != null) {
            for (AlertChannel channel : alertChannels) {
                register(channel);
            }
        }

        // 通过 SPI 机制加载额外渠道
        loadViaSpi();

        log.info("告警渠道初始化完成, 共加载 {} 个渠道: {}",
                channelCache.size(), channelCache.keySet());
    }

    /**
     * 注册告警渠道
     */
    public void register(AlertChannel channel) {
        if (channel == null || channel.getChannelId() == null) {
            log.warn("忽略无效的告警渠道: null");
            return;
        }

        String channelId = channel.getChannelId();
        if (channelCache.containsKey(channelId)) {
            log.warn("告警渠道已存在, 将被覆盖: {}", channelId);
        }

        channelCache.put(channelId, channel);
        log.debug("告警渠道注册成功: {} - {}", channelId, channel.getChannelName());
    }

    /**
     * 通过 SPI 机制加载渠道
     */
    private void loadViaSpi() {
        try {
            java.util.ServiceLoader<AlertChannel> loader =
                    java.util.ServiceLoader.load(AlertChannel.class);

            for (AlertChannel channel : loader) {
                if (!channelCache.containsKey(channel.getChannelId())) {
                    register(channel);
                    log.info("通过SPI加载告警渠道: {} - {}", channel.getChannelId(), channel.getChannelName());
                }
            }
        } catch (Exception e) {
            log.warn("通过SPI加载告警渠道失败: {}", e.getMessage());
        }
    }

    /**
     * 获取指定渠道
     */
    public AlertChannel getChannel(String channelId) {
        return channelCache.get(channelId);
    }

    /**
     * 获取所有已注册的渠道
     */
    public List<AlertChannel> getAllChannels() {
        return new ArrayList<>(channelCache.values());
    }

    /**
     * 获取指定配置下所有启用的渠道
     */
    public List<AlertChannel> getEnabledChannels(AlertConfig config) {
        if (config == null || !Boolean.TRUE.equals(config.getEnabled())) {
            return new ArrayList<>();
        }

        List<String> enabledChannelIds = config.getChannels();
        if (enabledChannelIds == null || enabledChannelIds.isEmpty()) {
            // 如果没有指定渠道，返回所有渠道
            return getAllChannels();
        }

        List<AlertChannel> enabledChannels = new ArrayList<>();
        for (String channelId : enabledChannelIds) {
            AlertChannel channel = channelCache.get(channelId);
            if (channel != null && channel.isAvailable(config)) {
                enabledChannels.add(channel);
            }
        }

        // 按优先级排序
        enabledChannels.sort((a, b) -> Integer.compare(a.getPriority(), b.getPriority()));

        return enabledChannels;
    }

    /**
     * 检查渠道是否已注册
     */
    public boolean hasChannel(String channelId) {
        return channelCache.containsKey(channelId);
    }

    /**
     * 获取已注册的渠道数量
     */
    public int getChannelCount() {
        return channelCache.size();
    }
}
