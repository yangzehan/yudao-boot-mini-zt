-- 数据中台 - 文件管理表结构
-- 创建时间: 2025-11-17

-- 创建文件管理表
CREATE TABLE IF NOT EXISTS `data_studio_file_manage` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '文件ID',
    `name` varchar(100) NOT NULL COMMENT '文件/文件夹名称',
    `type` varchar(20) NOT NULL DEFAULT 'file' COMMENT '文件类型：folder-文件夹，sql-sql文件，file-普通文件',
    `parent_id` bigint DEFAULT '0' COMMENT '父文件夹ID',
    `file_path` varchar(500) DEFAULT NULL COMMENT '文件路径',
    `content` longtext COMMENT '文件内容（仅对文件有效，文件夹为空）',
    `sort` int NOT NULL DEFAULT '0' COMMENT '显示顺序',
    `file_size` bigint DEFAULT '0' COMMENT '文件大小（字节）',
    `status` int NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
    `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
    `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_parent_id` (`parent_id`) USING BTREE,
    KEY `idx_type` (`type`) USING BTREE,
    KEY `idx_name` (`name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件管理表';
