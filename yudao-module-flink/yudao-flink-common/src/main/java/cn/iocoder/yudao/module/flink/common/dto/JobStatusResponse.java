package cn.iocoder.yudao.module.flink.common.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * @author yzh
 */
@Setter
@Getter
public class JobStatusResponse {
  private String jobId;
  private String jobName;
  private String status;
  private String statusDesc;
  private LocalDateTime startTime;
  private LocalDateTime updateTime;
  private long duration;
  private int parallelism;
  private Object lastError;
}
