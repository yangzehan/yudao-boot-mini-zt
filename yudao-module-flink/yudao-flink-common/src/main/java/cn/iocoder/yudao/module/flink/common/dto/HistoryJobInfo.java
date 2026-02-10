package cn.iocoder.yudao.module.flink.common.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Flink 历史作业信息 DTO
 *
 * <p>用于存储从 History Server 获取的历史作业信息，包括已完成、失败、取消的作业。
 *
 * @author yzh
 */
@Setter
@Getter
@Builder
public class HistoryJobInfo {

  /** 作业ID */
  private String jobId;

  /** 作业名称 */
  private String jobName;

  /** 作业状态: FINISHED, FAILED, CANCELED, RUNNING */
  private String state;

  /** 开始时间戳 */
  private long startTime;

  /** 结束时间戳 */
  private long endTime;

  /** 运行 duration (毫秒) */
  private long duration;

  /** 并行度 */
  private int parallelism;

  /** 最后错误信息 */
  private String lastError;
}
