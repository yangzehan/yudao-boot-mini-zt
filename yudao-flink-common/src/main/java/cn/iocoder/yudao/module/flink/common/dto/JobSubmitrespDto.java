package cn.iocoder.yudao.module.flink.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Flink作业提交响应DTO
 *
 * @author yzh
 * @since 2025-11-27
 */
@Data
public class JobSubmitrespDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 作业ID
     */
    private String jobId;

    /**
     * 作业名称
     */
    private String jobName;

    /**
     * 提交状态
     */
    private String submitStatus;

    /**
     * 提交时间
     */
    private LocalDateTime submitTime;

    /**
     * 消息
     */
    private String message;
}
