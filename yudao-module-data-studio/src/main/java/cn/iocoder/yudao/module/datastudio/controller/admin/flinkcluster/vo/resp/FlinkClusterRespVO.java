package cn.iocoder.yudao.module.datastudio.controller.admin.flinkcluster.vo.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Flink 集群配置响应 VO
 *
 * @author 芋道源码
 */
@Data
@Schema(description = "Flink 集群配置")
public class FlinkClusterRespVO {

    @Schema(description = "集群ID")
    private Long id;

    @Schema(description = "集群名称")
    private String name;

    @Schema(description = "集群类型：remote-远程集群，yarn-Flink on Yarn")
    private String type;

    @Schema(description = "集群状态：running-运行中，stopped-已停止，available-可用，unavailable-不可用")
    private String status;

    @Schema(description = "集群描述")
    private String description;

    @Schema(description = "标签列表")
    private List<String> tags;

    @Schema(description = "创建人")
    private String creator;

    @Schema(description = "负责人")
    private String owner;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "Flink 版本")
    private String flinkVersion;

    @Schema(description = "远程集群地址（remote类型）")
    private String remoteUrl;

    @Schema(description = "Web UI访问地址")
    private String webUiUrl;

    @Schema(description = "是否启用HA")
    private Boolean haEnabled;

    @Schema(description = "ZK命名空间")
    private String zkNamespace;

    @Schema(description = "Yarn ResourceManager地址（yarn类型）")
    private String yarnUrl;

    @Schema(description = "YARN队列名称")
    private String queueName;

    @Schema(description = "部署模式：session-per job-application")
    private String deployMode;

    @Schema(description = "Hadoop版本")
    private String hadoopVersion;

    @Schema(description = "集群总内存（MB）")
    private Integer memoryMB;

    @Schema(description = "CPU核心数")
    private Integer vcores;

    @Schema(description = "最大并行度")
    private Integer maxParallelism;

    @Schema(description = "连接超时时间（毫秒）")
    private Integer connectTimeout;

    @Schema(description = "心跳检测间隔（秒）")
    private Integer heartbeatInterval;

    @Schema(description = "最后连接时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastConnectedTime;

    @Schema(description = "告警配置（JSON格式）")
    private String alertConfig;

    @Schema(description = "自定义配置（JSON格式）")
    private String customConfig;

    @Schema(description = "关联项目列表")
    private List<String> projects;

}
