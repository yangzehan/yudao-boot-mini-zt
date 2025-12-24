package cn.iocoder.yudao.module.datastudio.api.job.dto;

import cn.iocoder.yudao.module.datastudio.api.enums.JobStatus;
import lombok.Data;

/**
 * @author yzh
 */
@Data
public class DataJobListReqVo {
  private JobStatus status;
  private String flinkVersion;
}
