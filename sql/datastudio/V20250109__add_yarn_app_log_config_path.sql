-- 新增应用日志配置文件路径字段
-- 用于 Flink on YARN Application 模式下指定应用的日志配置文件路径

ALTER TABLE `data_studio_flink_cluster`
    ADD COLUMN `yarn_app_log_config_path` varchar(512) DEFAULT NULL COMMENT '应用日志配置文件路径' AFTER `yarn_flink_dist_jar`;
