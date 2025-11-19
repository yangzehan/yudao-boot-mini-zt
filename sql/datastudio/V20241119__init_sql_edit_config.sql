-- 创建数据中台SQL编辑器配置表
CREATE TABLE IF NOT EXISTS `data_studio_sql_edit_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `sql_edit_id` bigint NOT NULL COMMENT 'SQL编辑器文件ID',
  `config` longtext NOT NULL COMMENT 'Flink配置信息JSON',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_sql_edit_id` (`sql_edit_id`) COMMENT 'SQL编辑器文件ID索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SQL编辑器配置表';
