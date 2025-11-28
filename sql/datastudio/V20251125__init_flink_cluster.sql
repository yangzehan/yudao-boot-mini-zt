-- Flink 集群配置表
CREATE TABLE `data_studio_flink_cluster` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '集群ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户编号',
  `name` varchar(64) NOT NULL COMMENT '集群名称',
  `type` varchar(32) NOT NULL COMMENT '集群类型：remote-远程集群，yarn-Flink on Yarn',
  `status` varchar(32) NOT NULL DEFAULT 'stopped' COMMENT '集群状态：running-运行中，stopped-已停止，available-可用，unavailable-不可用',
  `description` varchar(500) DEFAULT NULL COMMENT '集群描述',
  `tags` text COMMENT '标签列表（JSON格式）',
  `owner` varchar(64) DEFAULT NULL COMMENT '负责人',
  `flink_version` varchar(32) DEFAULT NULL COMMENT 'Flink版本',
  `remote_url` varchar(256) DEFAULT NULL COMMENT '远程集群地址（remote类型）',
    `memory_m_b` bigint DEFAULT NULL COMMENT '内存大小MB',
  `web_ui_url` varchar(256) DEFAULT NULL COMMENT 'Web UI访问地址',
  `ha_enabled` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否启用HA',
  `zk_namespace` varchar(128) DEFAULT NULL COMMENT 'ZK命名空间',
  `yarn_url` varchar(256) DEFAULT NULL COMMENT 'Yarn ResourceManager地址（yarn类型）',
  `queue_name` varchar(128) DEFAULT NULL COMMENT 'YARN队列名称',
  `deploy_mode` varchar(32) DEFAULT NULL COMMENT '部署模式：session-per job-application',
  `hadoop_version` varchar(32) DEFAULT NULL COMMENT 'Hadoop版本',
  `memory_mb` int DEFAULT NULL COMMENT '集群总内存（MB）',
  `vcores` int DEFAULT NULL COMMENT 'CPU核心数',
  `max_parallelism` int DEFAULT NULL COMMENT '最大并行度',
  `connect_timeout` int NOT NULL DEFAULT '30000' COMMENT '连接超时时间（毫秒）',
  `heartbeat_interval` int NOT NULL DEFAULT '60' COMMENT '心跳检测间隔（秒）',
  `last_connected_time` varchar(32) DEFAULT NULL COMMENT '最后连接时间',
  `alert_config` text COMMENT '告警配置（JSON格式）',
  `custom_config` text COMMENT '自定义配置（JSON格式）',
  `projects` text COMMENT '关联项目列表（JSON格式）',
  `creator` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updater` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_name` (`name`, `tenant_id`) USING BTREE,
  KEY `idx_type` (`type`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_create_time` (`create_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Flink 集群配置表';

-- Flink 集群配置表序列
CREATE SEQUENCE `data_studio_flink_cluster_seq` START WITH 1 INCREMENT BY 1 CACHE 1;

-- 插入测试数据（远程集群）
INSERT INTO `data_studio_flink_cluster` (
  `id`, `tenant_id`, `name`, `type`, `status`, `description`, `tags`, `owner`,
  `flink_version`, `remote_url`, `web_ui_url`, `ha_enabled`, `zk_namespace`,
  `memory_mb`, `vcores`, `max_parallelism`, `connect_timeout`, `heartbeat_interval`,
  `creator`, `updater`
) VALUES (
  1, 1, 'Flink生产集群', 'remote', 'running', '主要用于生产环境的Flink远程集群',
  '["生产", "核心"]', '张三',
  '1.17', '10.1.1.100:8081', 'http://10.1.1.100:8081', 1, 'flink_prod',
  32768, 16, 1000, 30000, 60,
  'admin','admin'
);

-- 插入测试数据（测试集群）
INSERT INTO `data_studio_flink_cluster` (
  `id`, `tenant_id`, `name`, `type`, `status`, `description`, `tags`, `owner`,
  `flink_version`, `remote_url`, `web_ui_url`, `ha_enabled`,
  `memory_mb`, `vcores`, `max_parallelism`, `connect_timeout`, `heartbeat_interval`,
  `creator`, `updater`
) VALUES (
  2, 1, 'Flink测试集群', 'remote', 'stopped', '测试环境使用的集群',
  '["测试"]', '李四',
  '1.18', '10.1.1.101:8081', 'http://10.1.1.101:8081', 0,
  16384, 8, 500, 30000, 60,
  'admin', 'admin'
);

-- 插入测试数据（Yarn集群）
INSERT INTO `data_studio_flink_cluster` (
  `id`, `tenant_id`, `name`, `type`, `status`, `description`, `tags`, `owner`,
  `flink_version`, `yarn_url`, `queue_name`, `deploy_mode`, `hadoop_version`,
  `memory_mb`, `vcores`, `max_parallelism`, `connect_timeout`, `heartbeat_interval`,
  `creator`, `updater`
) VALUES (
  3, 1, 'Flink on Yarn集群', 'yarn', 'available', '基于YARN的Flink集群',
  '["开发", "YARN"]', '王五',
  '1.17', 'http://10.1.1.102:8088', 'default', 'session', '3.3.0',
  8192, 4, 1000, 30000, 60,
  'admin', 'admin'
);

-- 插入测试数据（开发集群）
INSERT INTO `data_studio_flink_cluster` (
  `id`, `tenant_id`, `name`, `type`, `status`, `description`, `tags`,  `owner`,
  `flink_version`, `yarn_url`, `queue_name`, `deploy_mode`, `hadoop_version`,
  `memory_mb`, `vcores`, `max_parallelism`, `connect_timeout`, `heartbeat_interval`,
  `creator`, `updater`
) VALUES (
  4, 1, 'Flink开发集群', 'yarn', 'available', '开发环境使用的集群',
  '["开发", "测试"]', '赵六',
  '1.18', 'http://10.1.1.103:8088', 'flink_dev', 'per-job', '3.3.0',
  4096, 2, 200, 30000, 60,
  'admin', 'admin'
);
