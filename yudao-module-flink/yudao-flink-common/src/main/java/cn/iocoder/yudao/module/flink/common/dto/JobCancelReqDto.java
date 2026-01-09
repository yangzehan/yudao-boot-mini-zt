package cn.iocoder.yudao.module.flink.common.dto;

import lombok.Data;

import java.util.Map;

/**
 * 取消flink作业请求参数
 *
 * @author yzh
 */
@Data
public class JobCancelReqDto {
  private String jobId;

  private Map<String, String> config;
}
