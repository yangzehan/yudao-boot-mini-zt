package cn.iocoder.yudao.module.flink.common.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Data;
import org.springframework.lang.Nullable;

/**
 * Flink作业提交响应DTO
 *
 * @author yzh
 * @since 2025-11-27
 */
@Data
public class JobDeployRespDto implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 作业ID */
  private String jobId;

  /** 作业名称 */
  private String jobName;

  /** 提交状态 */
  private Boolean submitStatus;

  /** 提交时间 */
  private LocalDateTime submitTime;

  /** 消息 */
  private String message;

  private Map<String, String> config;
  private String deployMode;
  private String webInterfaceUrl;

  @Nullable private String flinkClusterId;
}
