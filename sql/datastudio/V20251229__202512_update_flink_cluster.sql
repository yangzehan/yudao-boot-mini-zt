-- 数据工作室 - Flink 集群表结构更新
-- 创建时间: 2025-12-29
-- 描述: 移除 hadoop_version 和 yarn_url 字段，新增 yarn_site_path、hdfs_site_path、core_site_path 字段
-- 更新: 2025-12-29 移除 memory_mb 字段，新增 jobmanager_memory_process_size、taskmanager_memory_process_size、taskmanager_number_of_task_slots 字段

-- 移除 yarn_url 字段
ALTER TABLE `data_studio_flink_cluster`
DROP COLUMN `yarn_url`;

-- 移除 hadoop_version 字段
ALTER TABLE `data_studio_flink_cluster`
DROP COLUMN `hadoop_version`;

-- 添加 yarn_site_path 字段
ALTER TABLE `data_studio_flink_cluster`
ADD COLUMN `yarn_site_path` varchar(512) NULL COMMENT 'Yarn配置文件路径' AFTER `queue_name`;

-- 添加 hdfs_site_path 字段
ALTER TABLE `data_studio_flink_cluster`
ADD COLUMN `hdfs_site_path` varchar(512) NULL COMMENT 'HDFS配置文件路径' AFTER `yarn_site_path`;

-- 添加 core_site_path 字段
ALTER TABLE `data_studio_flink_cluster`
ADD COLUMN `core_site_path` varchar(512) NULL COMMENT 'Core配置文件路径' AFTER `hdfs_site_path`;

-- 更新现有Yarn集群测试数据，设置配置文件路径默认值
UPDATE `data_studio_flink_cluster`
SET
  `yarn_site_path` = '/etc/hadoop/conf/yarn-site.xml',
  `hdfs_site_path` = '/etc/hadoop/conf/hdfs-site.xml',
  `core_site_path` = '/etc/hadoop/conf/core-site.xml'
WHERE `type` = 'yarn';

-- ============================================
-- 内存字段拆分更新 - 2025-12-29
-- ============================================

-- 移除旧的 memory_mb 字段
ALTER TABLE `data_studio_flink_cluster`
DROP COLUMN `memory_mb`;

-- 添加 jobmanager_memory_process_size 字段
ALTER TABLE `data_studio_flink_cluster`
ADD COLUMN `jobmanager_memory_process_size` int NULL COMMENT 'JobManager 进程总内存（MB）' AFTER `deploy_mode`;

-- 添加 taskmanager_memory_process_size 字段
ALTER TABLE `data_studio_flink_cluster`
ADD COLUMN `taskmanager_memory_process_size` int NULL COMMENT 'TaskManager 进程总内存（MB）' AFTER `jobmanager_memory_process_size`;

-- 添加 taskmanager_number_of_task_slots 字段
ALTER TABLE `data_studio_flink_cluster`
ADD COLUMN `taskmanager_number_of_task_slots` int NULL COMMENT 'TaskManager Slot 数量' AFTER `taskmanager_memory_process_size`;

-- 更新现有Yarn集群数据，设置内存字段默认值
UPDATE `data_studio_flink_cluster`
SET
  `jobmanager_memory_process_size` = 1600,
  `taskmanager_memory_process_size` = 4096,
  `taskmanager_number_of_task_slots` = 2
WHERE `type` = 'yarn' AND (`jobmanager_memory_process_size` IS NULL OR `taskmanager_memory_process_size` IS NULL OR `taskmanager_number_of_task_slots` IS NULL);
