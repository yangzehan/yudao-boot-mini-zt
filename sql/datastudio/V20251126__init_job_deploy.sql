-- 任务执行相关表
-- 创建时间：2025-11-26

-- 1. 任务执行记录表
create table data_studio_flink_job_deploy
(
    id                  bigint auto_increment comment '执行ID'
        primary key,
    tenant_id           bigint                                   null comment '租户ID',
    file_id             bigint                                   null comment '文件ID',
    cluster_id          bigint                                   null comment '集群ID',
    cluster_name        varchar(255)                             null comment '集群名称',
    execution_mode      varchar(20)                              not null comment '执行模式：stream/batch',
    flink_version       varchar(10)                              null comment 'Flink版本',
    job_id              varchar(100)                             null comment 'Flink作业ID',
    job_name            varchar(255)                             null comment '作业名称',
    status              varchar(20) default 'pending'            not null comment '状态：pending-等待中，running-运行中，succeeded-成功，failed-失败，cancelled-已取消',
    submit_time         datetime(3)                              not null comment '提交时间',
    start_time          datetime(3)                              null comment '开始时间',
    end_time            datetime(3)                              null comment '结束时间',
    duration            bigint                                   null comment '执行时长（毫秒）',
    parallelism         int                                      null comment '并行度',
    checkpoint_interval bigint                                   null comment '检查点间隔（毫秒）',
    error_message       text                                     null comment '错误信息',
    web_ui_url          varchar(500)                             null comment 'Flink Web UI链接',
    config              text                                     not null comment '配置信息',
    creator             varchar(64)                              null comment '创建者',
    create_time         datetime(3) default CURRENT_TIMESTAMP(3) null comment '创建时间',
    updater             varchar(64)                              null comment '更新者',
    update_time         datetime(3) default CURRENT_TIMESTAMP(3) null on update CURRENT_TIMESTAMP(3) comment '更新时间',
    deleted             bit         default b'0'                 not null comment '是否删除',
    deploy_mode         varchar(50)                              not null comment '部署模式'
)
    comment '任务执行记录表' charset = utf8mb4;

create index idx_cluster_id
    on data_studio_flink_job_deploy (cluster_id);

create index idx_file_id
    on data_studio_flink_job_deploy (file_id);

create index idx_status
    on data_studio_flink_job_deploy (status);

create index idx_submit_time
    on data_studio_flink_job_deploy (submit_time);


alter table data_studio_flink_job_deploy
    add flink_cluster_id varchar(50) null comment 'fink集群id' after cluster_id;

alter table data_studio_flink_job_deploy
    add job_type varchar(50) null comment '作业类型';
