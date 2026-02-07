-- 数据中台 - 资源管理表
CREATE TABLE IF NOT EXISTS `data_studio_resource` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '资源ID',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    `name` varchar(255) NOT NULL COMMENT '资源名称',
    `path` varchar(500) NOT NULL COMMENT '资源路径',
    `size` bigint NOT NULL COMMENT '文件大小（字节）',
    `description` varchar(500) DEFAULT NULL COMMENT '资源描述',
    `file_url` varchar(500) DEFAULT NULL COMMENT '文件访问URL',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',

    `create_by` varchar(255) DEFAULT NULL COMMENT '创建者',
    `update_by` varchar(255) DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (`id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据中台-资源管理表';

-- 数据中台 - 资源管理序列
CREATE SEQUENCE IF NOT EXISTS `data_studio_resource_seq`
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
