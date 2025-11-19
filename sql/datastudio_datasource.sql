-- 数据源配置表
CREATE TABLE `data_studio_datasource` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '数据源ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户编号',
  `name` varchar(64) NOT NULL COMMENT '数据源名称',
  `type` varchar(32) NOT NULL COMMENT '数据源类型：mysql- MySQL，postgresql- PostgreSQL，oracle- Oracle，sqlserver- SQL Server，clickhouse- ClickHouse，hive- Hive',
  `driver_class_name` varchar(128) DEFAULT NULL COMMENT '数据库驱动类名',
  `url` varchar(512) DEFAULT NULL COMMENT '连接URL',
  `host` varchar(128) DEFAULT NULL COMMENT '主机地址',
  `port` int DEFAULT NULL COMMENT '端口号',
  `database` varchar(64) DEFAULT NULL COMMENT '数据库名称',
  `username` varchar(64) DEFAULT NULL COMMENT '用户名',
  `password` varchar(128) DEFAULT NULL COMMENT '密码（加密存储）',
  `connection_params` text COMMENT '连接参数（JSON格式）',
  `status` int NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `connection_status` varchar(32) DEFAULT 'disconnected' COMMENT '连接状态：connecting-连接中，connected-已连接，disconnected-未连接，error-连接错误',
  `last_connection_time` bigint DEFAULT NULL COMMENT '连接测试时间',
  `last_connection_error` text COMMENT '连接错误信息',
  `sort` int NOT NULL DEFAULT '0' COMMENT '显示顺序',
  `description` varchar(256) DEFAULT NULL COMMENT '描述信息',
  `ext_info` text COMMENT '扩展信息（JSON格式）',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据源配置表';

-- 数据源配置表序列
CREATE SEQUENCE `data_studio_datasource_seq` START WITH 1 INCREMENT BY 1 CACHE 1;

-- 插入测试数据（MySQL）
INSERT INTO `data_studio_datasource` (`id`, `tenant_id`, `name`, `type`, `driver_class_name`, `url`, `host`, `port`, `database`, `username`, `status`, `connection_status`, `sort`, `description`, `creator`, `updater`)
VALUES
(1, 1, 'MySQL测试数据库', 'mysql', 'com.mysql.cj.jdbc.Driver', 'jdbc:mysql://localhost:3306/test_db?useUnicode=true&characterEncoding=UTF-8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai', 'localhost', 3306, 'test_db', 'root', 1, 'disconnected', 1, '用于测试的MySQL数据库', 'admin', 'admin');

-- 插入测试数据（PostgreSQL）
INSERT INTO `data_studio_datasource` (`id`, `tenant_id`, `name`, `type`, `driver_class_name`, `url`, `host`, `port`, `database`, `username`, `status`, `connection_status`, `sort`, `description`, `creator`, `updater`)
VALUES
(2, 1, 'PostgreSQL生产库', 'postgresql', 'org.postgresql.Driver', 'jdbc:postgresql://192.168.1.100:5432/production_db', '192.168.1.100', 5432, 'production_db', 'postgres', 1, 'disconnected', 2, '生产环境PostgreSQL数据库', 'admin', 'admin');

-- 插入测试数据（Hive）
INSERT INTO `data_studio_datasource` (`id`, `tenant_id`, `name`, `type`, `driver_class_name`, `url`, `host`, `port`, `database`, `username`, `status`, `connection_status`, `sort`, `description`, `creator`, `updater`)
VALUES
(3, 1, 'Hive数据仓库', 'hive', 'org.apache.hive.jdbc.HiveDriver', 'jdbc:hive2://hive-server:10000/warehouse', 'hive-server', 10000, 'warehouse', 'hive', 1, 'disconnected', 3, 'Hive数据仓库', 'admin', 'admin');
