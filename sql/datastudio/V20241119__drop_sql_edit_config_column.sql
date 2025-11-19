-- 修改 data_studio_sql_edit 表，移除 config 列（已迁移到独立表）
alter table data_studio_sql_edit
    drop column `config`;

