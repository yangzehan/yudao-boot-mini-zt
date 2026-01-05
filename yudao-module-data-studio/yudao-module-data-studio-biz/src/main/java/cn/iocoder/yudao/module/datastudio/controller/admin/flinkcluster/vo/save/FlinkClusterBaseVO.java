package cn.iocoder.yudao.module.datastudio.controller.admin.flinkcluster.vo.save;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * Flink 集群配置基础保存 VO
 *
 * @author 芋道源码
 */
@Data
@Schema(description = "Flink 集群配置")
public class FlinkClusterBaseVO {

  @Schema(description = "集群名称", required = true, example = "测试集群")
  @NotBlank(message = "集群名称不能为空")
  @Size(min = 2, max = 50, message = "集群名称长度在 2 到 50 个字符")
  private String name;

  @Schema(description = "集群类型：remote-远程集群，yarn-Flink on Yarn", required = true, example = "remote")
  @NotNull(message = "集群类型不能为空")
  private String type;

  @Schema(
      description = "集群状态：running-运行中，stopped-已停止，available-可用，unavailable-不可用",
      example = "stopped")
  private String status;

  @Schema(description = "集群描述", example = "这是我的测试集群")
  @Size(max = 500, message = "描述长度不能超过 500 个字符")
  private String description;

  @Schema(description = "标签列表")
  private List<String> tags;

  @Schema(description = "负责人", example = "张三")
  private String owner;

  @Schema(description = "Flink 版本", example = "1.18")
  private String flinkVersion;

  @Schema(description = "远程集群地址（remote类型）", example = "10.1.1.100:8081")
  private String remoteUrl;

  @Schema(description = "Web UI访问地址", example = "http://10.1.1.100:8081")
  private String webUiUrl;

  @Schema(description = "是否启用HA", example = "true")
  private Boolean haEnabled;

  @Schema(description = "ZK命名空间", example = "flink_prod")
  private String zkNamespace;

  @Schema(description = "YARN队列名称", example = "default")
  private String queueName;

  @Schema(description = "Yarn配置文件路径", example = "/etc/hadoop/conf/yarn-site.xml")
  private String yarnSitePath;

  @Schema(description = "HDFS配置文件路径", example = "/etc/hadoop/conf/hdfs-site.xml")
  private String hdfsSitePath;

  @Schema(description = "Core配置文件路径", example = "/etc/hadoop/conf/core-site.xml")
  private String coreSitePath;

  @Schema(
      description = "YARN提供的lib目录（多个目录用分号分隔）",
      example = "hdfs://namenode:8020/lib/flink;hdfs://namenode:8020/lib/custom")
  private String yarnProvidedLibDirs;

  @Schema(description = "用户自定义lib目录", example = "hdfs://namenode:8020/userlib")
  private String yarnProvidedUsrLibDir;

  @Schema(description = "Flink分布式jar包路径", example = "hdfs://namenode:8020/flink-dist/flink-dist-1.18.jar")
  private String yarnFlinkDistJar;

  @Schema(description = "部署模式：session-per job-application", example = "session")
  private String deployMode;

  @Schema(description = "JobManager 进程总内存（MB）", example = "1600")
  private Integer jobmanagerMemoryProcessSize;

  @Schema(description = "TaskManager 进程总内存（MB）", example = "4096")
  private Integer taskmanagerMemoryProcessSize;

  @Schema(description = "TaskManager Slot 数量", example = "2")
  private Integer taskmanagerNumberOfTaskSlots;

  @Schema(description = "CPU核心数", example = "8")
  private Integer vcores;

  @Schema(description = "最大并行度", example = "1000")
  private Integer maxParallelism;

  @Schema(description = "连接超时时间（毫秒）", example = "30000")
  private Integer connectTimeout;

  @Schema(description = "心跳检测间隔（秒）", example = "60")
  private Integer heartbeatInterval;

  @Schema(description = "告警配置（JSON格式）")
  private String alertConfig;

  @Schema(description = "自定义配置（JSON格式）")
  private String customConfig;

  @Schema(description = "关联项目列表")
  private List<String> projects;
}
