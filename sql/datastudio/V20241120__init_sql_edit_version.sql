-- 创建 SQL 编辑器版本表
CREATE TABLE IF NOT EXISTS `data_studio_sql_edit_version` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '版本ID',
    `sql_edit_id` bigint NOT NULL COMMENT '文件ID（关联data_studio_sql_edit.id）',
    `version_number` bigint NOT NULL COMMENT '版本号（自增，通过应用层控制）',
    `content` longtext NOT NULL COMMENT '脚本内容快照',
    `config` longtext COMMENT 'Flink配置快照（JSON格式）',
    `remark` varchar(500) DEFAULT NULL COMMENT '版本备注',
    `version_type` varchar(20) NOT NULL DEFAULT 'manual' COMMENT '版本类型：manual-手动保存, auto-自动保存',
    `creator` varchar(64) NOT NULL COMMENT '创建者',
    `updater` varchar(64) NOT NULL COMMENT '更新者',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除标记',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_sql_edit_id` (`sql_edit_id`),
    KEY `idx_version_number` (`version_number`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SQL编辑器版本表';
