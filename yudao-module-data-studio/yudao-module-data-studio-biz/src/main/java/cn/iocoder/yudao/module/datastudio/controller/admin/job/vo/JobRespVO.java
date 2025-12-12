package cn.iocoder.yudao.module.datastudio.controller.admin.job.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.module.flink.common.enums.JobTypeEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 作业管理 Response VO
 *
 * @author 芋道源码
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(name = "JobRespVO", description = "作业管理 Response VO")
public class JobRespVO extends PageParam {

    @Schema(name = "id", description = "ID")
    private Long id;

    @Schema(name = "jobId", description = "Flink作业ID")
    private String jobId;

    @Schema(name = "jobName", description = "作业名称")
    private String jobName;

    @Schema(name = "jobType", description = "作业类型")
    private JobTypeEnum jobType;

    @Schema(name = "status", description = "作业状态")
    private String status;

    @Schema(name = "executionMode", description = "执行模式")
    private String executionMode;

    @Schema(name = "flinkVersion", description = "Flink版本")
    private String flinkVersion;

    @Schema(name = "parallelism", description = "并行度")
    private Integer parallelism;

    @Schema(name = "checkpointInterval", description = "检查点间隔(毫秒)")
    private Long checkpointInterval;

    @Schema(name = "clusterId", description = "集群ID")
    private Long clusterId;

    @Schema(name = "clusterName", description = "集群名称")
    private String clusterName;

    @Schema(name = "fileId", description = "文件ID")
    private Long fileId;

    @Schema(name = "config", description = "配置信息")
    private Map<String, String> config;

    @Schema(name = "errorMessage", description = "错误信息")
    private String errorMessage;

    @Schema(name = "webUiUrl", description = "Flink Web UI链接")
    private String webUiUrl;

    @Schema(name = "deployMode", description = "部署模式")
    private String deployMode;

    @Schema(name = "submitTime", description = "提交时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitTime;

    @Schema(name = "startTime", description = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(name = "endTime", description = "结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @Schema(name = "duration", description = "执行时长(毫秒)")
    private Long duration;

}
