-- 任务执行相关表
-- 创建时间：2025-11-26

-- 1. 任务执行记录表
CREATE TABLE `data_studio_job_execution` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '执行ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `file_id` bigint DEFAULT NULL COMMENT '文件ID',
  `file_name` varchar(255) NOT NULL COMMENT '文件名',
  `cluster_id` bigint DEFAULT NULL COMMENT '集群ID',
  `cluster_name` varchar(255) DEFAULT NULL COMMENT '集群名称',
  `execution_mode` varchar(20) NOT NULL COMMENT '执行模式：stream/batch',
  `flink_version` varchar(10) DEFAULT NULL COMMENT 'Flink版本',
  `job_id` varchar(100) DEFAULT NULL COMMENT 'Flink作业ID',
  `job_name` varchar(255) DEFAULT NULL COMMENT '作业名称',
  `status` varchar(20) NOT NULL DEFAULT 'pending' COMMENT '状态：pending-等待中，running-运行中，succeeded-成功，failed-失败，cancelled-已取消',
  `submit_time` datetime(3) NOT NULL COMMENT '提交时间',
  `start_time` datetime(3) DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime(3) DEFAULT NULL COMMENT '结束时间',
  `duration` bigint DEFAULT NULL COMMENT '执行时长（毫秒）',
  `parallelism` int DEFAULT NULL COMMENT '并行度',
  `checkpoint_interval` bigint DEFAULT NULL COMMENT '检查点间隔（毫秒）',
  `error_message` text COMMENT '错误信息',
  `web_ui_url` varchar(500) DEFAULT NULL COMMENT 'Flink Web UI链接',
  `creator` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updater` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_file_id` (`file_id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_submit_time` (`submit_time`) USING BTREE,
  KEY `idx_cluster_id` (`cluster_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务执行记录表';

-- 2. 执行结果表
CREATE TABLE `data_studio_job_result` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '结果ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `execution_id` bigint NOT NULL COMMENT '执行ID',
  `result_type` varchar(20) NOT NULL COMMENT '结果类型：table-表格，chart-图表，text-文本',
  `column_names` text COMMENT '列名列表（JSON格式）',
  `row_count` int DEFAULT '0' COMMENT '结果行数',
  `result_data` longtext COMMENT '结果数据（JSON格式）',
  `result_url` varchar(500) DEFAULT NULL COMMENT '结果文件URL（如HDFS）',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_execution_id` (`execution_id`) USING BTREE,
  CONSTRAINT `fk_job_result_execution` FOREIGN KEY (`execution_id`) REFERENCES `data_studio_job_execution` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='执行结果表';

-- 3. 集群版本映射表
CREATE TABLE `data_studio_cluster_version` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '映射ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `cluster_id` bigint NOT NULL COMMENT '集群ID',
  `flink_version` varchar(10) NOT NULL COMMENT 'Flink版本',
  `supported_modes` text COMMENT '支持的执行模式（JSON格式）',
  `rpc_endpoint` varchar(200) DEFAULT NULL COMMENT 'RPC服务地址',
  `is_active` bit(1) NOT NULL DEFAULT b'1' COMMENT '是否启用',
  `creator` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updater` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_cluster_id` (`cluster_id`) USING BTREE,
  KEY `idx_flink_version` (`flink_version`) USING BTREE,
  CONSTRAINT `fk_cluster_version_cluster` FOREIGN KEY (`cluster_id`) REFERENCES `data_studio_flink_cluster` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='集群版本映射表';

-- 4. 任务执行日志表
CREATE TABLE `data_studio_job_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `execution_id` bigint NOT NULL COMMENT '执行ID',
  `log_type` varchar(20) NOT NULL COMMENT '日志类型：jobmanager-JobManager，taskmanager-TaskManager，system-系统',
  `log_level` varchar(20) NOT NULL COMMENT '日志级别：INFO，WARN，ERROR，DEBUG',
  `log_content` longtext NOT NULL COMMENT '日志内容',
  `log_time` datetime(3) NOT NULL COMMENT '日志时间',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_execution_id` (`execution_id`) USING BTREE,
  KEY `idx_log_type` (`log_type`) USING BTREE,
  CONSTRAINT `fk_job_log_execution` FOREIGN KEY (`execution_id`) REFERENCES `data_studio_job_execution` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务执行日志表';
