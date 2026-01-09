package cn.iocoder.yudao.module.flink.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 提交SQL作业的参数
 *
 * @author yzh
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobDeploySqlReqDto {
  private String sql;
  private FlinkConfig flinkConfig;
  private Long clusterId;
  private String clusterName;
  private Long fileId;
  private String jobName;
}
