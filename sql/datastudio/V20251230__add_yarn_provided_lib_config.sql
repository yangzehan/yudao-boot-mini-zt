-- 新增 YARN provided lib 配置字段
-- 用于 Flink on YARN Application 模式下指定外部依赖 jar 包目录

ALTER TABLE `data_studio_flink_cluster`
    ADD COLUMN `yarn_provided_lib_dirs`    varchar(1024) DEFAULT NULL COMMENT 'YARN提供的lib目录（多个目录用逗号分隔）' AFTER `core_site_path`,
    ADD COLUMN `yarn_provided_usr_lib_dir` varchar(512)  DEFAULT NULL COMMENT '用户自定义lib目录' AFTER `yarn_provided_lib_dirs`,
    ADD COLUMN `yarn_flink_dist_jar`       varchar(512)  DEFAULT NULL COMMENT 'Flink分布式jar包路径' AFTER `yarn_provided_usr_lib_dir`;
