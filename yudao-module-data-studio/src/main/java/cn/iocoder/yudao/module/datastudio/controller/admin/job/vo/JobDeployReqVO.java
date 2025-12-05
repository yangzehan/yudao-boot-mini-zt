package cn.iocoder.yudao.module.datastudio.controller.admin.job.vo;

import cn.iocoder.yudao.module.flink.common.enums.JobTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 作业管理保存 Request VO
 *
 * @author 芋道源码
 */
@Data
@Schema(name = "JobDeployReqVO", description = "作业管理保存 Request VO")
public class JobDeployReqVO {

    @Schema(name = "jobName", description = "作业名称", required = true)
    @NotBlank(message = "作业名称不能为空")
    private String jobName;

    @Schema(name = "flinkVersion", description = "Flink版本")
    private String flinkVersion;

    @Schema(name = "parallelism", description = "并行度")
    private Integer parallelism;

    @Schema(name = "checkpointInterval", description = "检查点间隔(毫秒)")
    private Long checkpointInterval;

    @Schema(name = "clusterId", description = "集群ID")
    private Long clusterId;


    @Schema(name = "fileId", description = "文件ID")
    private Long fileId;

    @Schema(name = "remark", description = "备注")
    private String remark;

    @Schema(name = "jobType", description = "创建时间")
    private JobTypeEnum jobType;
    @Schema(name = "jarFile", description = "Jar文件")
    private String jarFile;
    @Schema(name = "entryPointClassName", description = "入口类名")
    private String entryPointClassName;
    @Schema(name = "arguments", description = "参数")
    private String[] arguments;

    @Schema(name = "deployMode", description = "部署模式")
    private String deployMode;

}
