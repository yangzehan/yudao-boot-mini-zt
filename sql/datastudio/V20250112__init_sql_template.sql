-- SQL 模板表
CREATE TABLE `data_studio_sql_template` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户编号',
  `name` varchar(128) NOT NULL COMMENT '模板名称',
  `description` varchar(500) DEFAULT NULL COMMENT '模板描述',
  `category` varchar(64) NOT NULL COMMENT '模板分类：datastream-datastream作业，datasource-数据源，sql-通用SQL，etl-数据清洗，alert-告警',
  `content` text NOT NULL COMMENT 'SQL模板内容（支持占位符 ${table} 等）',
  `default_config` text COMMENT '默认Flink配置（JSON格式）',
  `sort` int NOT NULL DEFAULT '0' COMMENT '显示顺序',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `creator` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updater` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_category` (`category`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_create_time` (`create_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SQL 模板表';

-- SQL 模板表序列
CREATE SEQUENCE `data_studio_sql_template_seq` START WITH 1 INCREMENT BY 1 CACHE 1;

-- 初始化示例模板数据
INSERT INTO `data_studio_sql_template` (
  `id`, `tenant_id`, `name`, `description`, `category`, `content`, `default_config`, `sort`, `status`, `creator`, `updater`
) VALUES
-- Flink SQL 模板
(1, 1, 'Flink SQL 基础模板', 'Flink SQL 基础开发模板，包含 CTE 和窗口函数示例', 'sql',
'-- Flink SQL 基础模板
-- 说明：本模板演示 Flink SQL 的基本用法

-- 1. 创建源表（以 Kafka 为例）
CREATE TABLE source_table (
    id BIGINT,
    name STRING,
    amount BIGINT,
    row_time AS TO_TIMESTAMP(FROM_UNIXTIME(event_time)),
    WATERMARK FOR row_time AS row_time - INTERVAL ''5'' SECOND
) WITH (
    ''connector'' = ''kafka'',
    ''topic'' = ''${kafka_topic}'',
    ''properties.bootstrap.servers'' = ''${kafka_servers}'',
    ''properties.group.id'' = ''${kafka_group_id}'',
    ''format'' = ''json''
);

-- 2. 创建维表（以 MySQL 为例）
CREATE TABLE dim_table (
    id BIGINT,
    name STRING,
    category STRING,
    PRIMARY KEY (id) NOT ENFORCED
) WITH (
    ''connector'' = ''jdbc'',
    ''url'' = ''jdbc:mysql://${mysql_host}:${mysql_port}/${mysql_database}'',
    ''username'' = ''${mysql_username}'',
    ''password'' = ''${mysql_password}'',
    ''table-name'' = ''dim_table''
);

-- 3. 创建结果表
CREATE TABLE sink_table (
    id BIGINT,
    name STRING,
    total_amount BIGINT,
    window_start TIMESTAMP,
    window_end TIMESTAMP
) WITH (
    ''connector'' = ''jdbc'',
    ''url'' = ''jdbc:mysql://${mysql_host}:${mysql_port}/${mysql_database}'',
    ''username'' = ''${mysql_username}'',
    ''password'' = ''${mysql_password}'',
    ''table-name'' = ''sink_table''
);

-- 4. 业务逻辑：关联维表并按窗口聚合
INSERT INTO sink_table
SELECT
    s.id,
    d.name,
    SUM(s.amount) AS total_amount,
    TUMBLE_START(s.row_time, INTERVAL ''1'' HOUR) AS window_start,
    TUMBLE_END(s.row_time, INTERVAL ''1'' HOUR) AS window_end
FROM source_table s
LEFT JOIN dim_table d ON s.id = d.id
GROUP BY TUMBLE(s.row_time, INTERVAL ''1'' HOUR), s.id, d.name;',
'{"executionType": "streaming", "deployMode": "local", "flinkVersion": "1.16", "parallelism": 1, "checkpointInterval": 5000}',
1, 1, 'admin', 'admin'),

-- 数据清洗模板
(3, 1, 'ETL 数据清洗模板', '数据清洗和转换模板，包含数据过滤、转换、去重', 'etl',
'-- ETL 数据清洗模板
-- 功能：数据过滤、转换、去重

-- 源表
CREATE TABLE source_kafka (
    id BIGINT,
    name STRING,
    status STRING,
    amount BIGINT,
    create_time TIMESTAMP(3),
    proctime AS PROCTIME()
) WITH (
    ''connector'' = ''kafka'',
    ''topic'' = ''source_topic'',
    ''properties.bootstrap.servers'' = ''${kafka_servers}'',
    ''properties.group.id'' = ''etl_group'',
    ''format'' = ''json''
);

-- 清洗规则1：过滤无效数据
CREATE VIEW filtered_data AS
SELECT id, name, amount, create_time
FROM source_kafka
WHERE status IS NOT NULL
  AND LENGTH(TRIM(status)) > 0
  AND amount > 0;

-- 清洗规则2：数据转换
CREATE VIEW transformed_data AS
SELECT
    id,
    UPPER(name) AS name_upper,
    CASE
        WHEN status = ''A'' THEN ''Active''
        WHEN status = ''I'' THEN ''Inactive''
        ELSE ''Unknown''
    END AS status_name,
    amount * 100 AS amount_cent,
    DATE_FORMAT(create_time, ''yyyy-MM-dd'') AS date_str,
    proctime
FROM filtered_data;

-- 清洗规则3：去重
CREATE VIEW deduplicated_data AS
SELECT id, name_upper, status_name, amount_cent, date_str, proctime
FROM (
    SELECT *,
           ROW_NUMBER() OVER (PARTITION BY id ORDER BY proctime DESC) AS rn
    FROM transformed_data
) t
WHERE rn = 1;

-- 输出结果
CREATE TABLE sink_mysql (
    id BIGINT,
    name_upper STRING,
    status_name STRING,
    amount_cent BIGINT,
    date_str STRING,
    PRIMARY KEY (id) NOT ENFORCED
) WITH (
    ''connector'' = ''jdbc'',
    ''url'' = ''jdbc:mysql://${mysql_host}:${mysql_port}/${mysql_database}'',
    ''username'' = ''${mysql_username}'',
    ''password'' = ''${mysql_password}'',
    ''table-name'' = ''etl_result''
);

INSERT INTO sink_mysql
SELECT id, name_upper, status_name, amount_cent, date_str
FROM deduplicated_data;',
'{"executionType": "streaming", "deployMode": "local", "flinkVersion": "1.16", "parallelism": 1, "checkpointInterval": 60000}',
2, 1, 'admin', 'admin');
