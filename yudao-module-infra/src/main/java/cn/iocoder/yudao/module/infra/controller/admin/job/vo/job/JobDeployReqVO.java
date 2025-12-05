package cn.iocoder.yudao.module.infra.controller.admin.job.vo.job;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "管理后台 - 作业部署 Request VO")
@Data
public class JobDeployReqVO {

    @Schema(description = "作业类型：sql-Flink SQL, jar-JAR包, datasource-数据摄取", requiredMode = Schema.RequiredMode.REQUIRED, example = "sql")
    @NotEmpty(message = "作业类型不能为空")
    private String jobType;

    // ========== SQL/数据摄取任务字段 ==========

    @Schema(description = "任务ID（SQL/数据摄取任务使用）", example = "1024")
    private Long jobId;

    // ========== JAR 任务字段 ==========

    @Schema(description = "JAR 文件路径", example = "/upload/jar/my-flink-job.jar")
    private String jarFile;

    @Schema(description = "入口类名", example = "com.example.flink.MainJob")
    private String entryPointClassName;

    @Schema(description = "作业名称", example = "my-flink-job")
    private String jobName;

    @Schema(description = "作业参数列表")
    private List<String> arguments;

    @Schema(description = "部署模式：local-本地, remote-远程, yarn-application-YARN Application", example = "local")
    private String deployMode;

    @Schema(description = "集群ID", example = "1")
    private Long clusterId;

    @Schema(description = "Flink 版本", example = "1.18")
    private String flinkVersion;

    @Schema(description = "并行度", example = "1")
    private Integer parallelism;

    @Schema(description = "检查点间隔（毫秒）", example = "50000")
    private Long checkpointInterval;

}
