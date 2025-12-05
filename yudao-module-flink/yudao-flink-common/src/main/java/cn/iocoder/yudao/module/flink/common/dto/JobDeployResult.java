package cn.iocoder.yudao.module.flink.common.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Flink作业执行结果
 * 用于返回作业提交后的关键信息，便于后续管理和监控
 *
 * @author yzh
 * @since 2025-11-27
 */
@Data
public class JobDeployResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 作业ID
     */
    private String jobId;

    /**
     * 集群ID
     */
    private String clusterId;

    /**
     * Web界面URL，用于查看作业状态
     */
    private String webInterfaceUrl;

    /**
     * 执行模式（如：local、standalone、yarn、k8s等）
     */
    private String executionMode;

    /**
     * 执行是否成功
     */
    private Boolean success;

    /**
     * 消息
     */
    private String message;
}
