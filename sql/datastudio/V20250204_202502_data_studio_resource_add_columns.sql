-- 添加资源管理表缺失的 creator 和 updater 列
-- 用于支持芋道框架的 BaseDO 实体类

ALTER TABLE `data_studio_resource`
ADD COLUMN `creator` varchar(50) DEFAULT NULL COMMENT '创建者' AFTER `deleted`,
ADD COLUMN `updater` varchar(50) DEFAULT NULL COMMENT '更新者' AFTER `creator`;
