-- 数据摄取配置表
CREATE TABLE IF NOT EXISTS `data_studio_data_ingestion_config`
(
    `id`             bigint        NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `data_ingestion_id`          bigint        NOT NULL COMMENT '数据摄取文件ID',
    `config`         longtext     NOT NULL COMMENT '配置信息(JSON格式)',
    `creator` varchar(64) NOT NULL COMMENT '创建者',
    `updater` varchar(64) NOT NULL COMMENT '更新者',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除标记',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_data_ingestion_id` (`data_ingestion_id`) COMMENT '数据摄取文件ID唯一索引',
    KEY `idx_tenant_id` (`tenant_id`) COMMENT '租户编号索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '数据摄取配置表';
