package cn.iocoder.yudao.module.datastudio.dal.dataobject.flinkcluster;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.lang.Nullable;

/**
 * Flink 集群配置表
 *
 * @author 芋道源码
 */
@TableName("data_studio_flink_cluster")
@KeySequence("data_studio_flink_cluster_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class FlinkClusterDO extends TenantBaseDO {

  /** 集群ID */
  @TableId private Long id;

  /** 集群名称 */
  private String name;

  /** 集群类型：remote-远程集群，yarn-Flink on Yarn */
  private String type;

  /** 集群状态：running-运行中，stopped-已停止，available-可用，unavailable-不可用 */
  private String status;

  /** 集群描述 */
  private String description;

  /** 标签列表（JSON格式） */
  @TableField("`tags`")
  private String tags;

  /** 创建人 */
  private String creator;

  /** 负责人 */
  private String owner;

  /** Flink 版本 */
  @TableField("flink_version")
  private String flinkVersion;

  /** 远程集群地址（remote类型） */
  @TableField("remote_url")
  private String remoteUrl;

  /** Web UI访问地址 */
  @TableField("web_ui_url")
  private String webUiUrl;

  /** 是否启用HA */
  @TableField("ha_enabled")
  private Boolean haEnabled;

  /** ZK命名空间 */
  @TableField("zk_namespace")
  private String zkNamespace;

  /** YARN队列名称 */
  @TableField("queue_name")
  @Nullable
  private String queueName;

  /** Yarn配置文件路径 */
  @TableField("yarn_site_path")
  @Nullable
  private String yarnSitePath;

  /** HDFS配置文件路径 */
  @TableField("hdfs_site_path")
  @Nullable
  private String hdfsSitePath;

  /** Core配置文件路径 */
  @TableField("core_site_path")
  @Nullable
  private String coreSitePath;

  /** YARN提供的lib目录（多个目录用逗号分隔） */
  @TableField("yarn_provided_lib_dirs")
  @Nullable
  private String yarnProvidedLibDirs;

  /** 用户自定义lib目录 */
  @TableField("yarn_provided_usr_lib_dir")
  @Nullable
  private String yarnProvidedUsrLibDir;

  /** Flink分布式jar包路径 */
  @TableField("yarn_flink_dist_jar")
  @Nullable
  private String yarnFlinkDistJar;

  /** 应用日志配置文件路径 */
  @TableField("yarn_app_log_config_path")
  @Nullable
  private String yarnAppLogConfigPath;

  /** 部署模式：session-per job-application */
  @TableField("deploy_mode")
  private String deployMode;

  /** JobManager 进程总内存（MB） */
  @TableField("jobmanager_memory_process_size")
  @Nullable
  private Integer jobmanagerMemoryProcessSize;

  /** TaskManager 进程总内存（MB） */
  @TableField("taskmanager_memory_process_size")
  @Nullable
  private Integer taskmanagerMemoryProcessSize;

  /** TaskManager Slot 数量 */
  @TableField("taskmanager_number_of_task_slots")
  @Nullable
  private Integer taskmanagerNumberOfTaskSlots;

  /** CPU核心数 */
  private Integer vcores;

  /** 最大并行度 */
  @TableField("max_parallelism")
  private Integer maxParallelism;

  /** 连接超时时间（毫秒） */
  @TableField("connect_timeout")
  private Integer connectTimeout;

  /** 心跳检测间隔（秒） */
  @TableField("heartbeat_interval")
  private Integer heartbeatInterval;

  /** 最后连接时间 */
  @TableField("last_connected_time")
  private String lastConnectedTime;

  /** 告警配置（JSON格式） */
  @TableField("alert_config")
  private String alertConfig;

  /** 自定义配置（JSON格式） */
  @TableField("custom_config")
  private String customConfig;

  /** 关联项目列表（JSON格式） */
  @TableField("`projects`")
  private String projects;

  @TableField(exist = false)
  private Boolean deleted;
}
