package cn.iocoder.yudao.module.flink.common.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * Flink作业提交请求DTO
 *
 * @author yzh
 * @since 2025-11-27
 */
@Data
public class JobSubmitRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 作业名称
     */
    @NotBlank(message = "作业名称不能为空")
    private String jobName;

    /**
     * 主类路径
     */
    @NotBlank(message = "主类路径不能为空")
    private String mainClass;

    /**
     * 作业参数（JSON字符串）
     */
    private String jobArgs;

    /**
     * Flink配置参数（JSON字符串）
     */
    private String flinkConfig;

    /**
     * 并行度
     */
    private Integer parallelism;

    /**
     * 作业描述
     */
    private String description;
}
