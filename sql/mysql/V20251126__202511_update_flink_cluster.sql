-- 数据工作室 - Flink 集群表结构更新
-- 创建时间: 2025-11-26
-- 描述: 添加 last_heartbeat_time 字段，移除 auto_reconnect 字段

-- 更新 flink_cluster 表，添加 last_heartbeat_time 字段，移除 auto_reconnect 字段
ALTER TABLE `data_studio_flink_cluster`
ADD COLUMN `last_heartbeat_time` varchar(64) NULL COMMENT '最后心跳时间' AFTER `last_connected_time`;

-- 更新现有数据，设置默认的最后心跳时间
UPDATE `data_studio_flink_cluster`
SET `last_heartbeat_time` = NULL
WHERE `last_heartbeat_time` IS NULL;
