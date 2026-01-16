-- 为 SQL 模板表添加占位符配置字段
-- 日期: 2025-01-12

-- 1. 添加 placeholder_config 字段
ALTER TABLE `data_studio_sql_template`
ADD COLUMN `placeholder_config` text COMMENT '占位符配置（JSON格式，存储每个占位符的标签和提示信息）' AFTER `default_config`;

-- 2. 更新现有模板的占位符配置（可选，自动生成默认配置）
-- 这里不需要更新现有数据，前端会自动从模板内容解析占位符并使用默认标签/提示
