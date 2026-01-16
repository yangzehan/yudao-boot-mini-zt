package cn.iocoder.yudao.module.flink.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yzh
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobDeployDataIngestionReqDto {
  private String content;
  private FlinkConfig flinkConfig;
  private Long clusterId;
  private String clusterName;
  private Long fileId;
  private String jobName;
  /** Flink CDC Dist JAR包路径 - 用于Yarn Application模式下的CDC数据同步 */
  private String flinkCdcDistJarPath;
}
