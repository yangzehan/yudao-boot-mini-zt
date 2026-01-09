package cn.iocoder.yudao.module.flink.common.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 提交Jar作业的参数
 *
 * @author yzh
 */
@Setter
@Getter
public class JobDeployJarReqDto {
  private FlinkConfig flinkConfig;
  private String jarFile;
  private String entryPointClassName;
  private String[] args;
  private String jobName;
}
