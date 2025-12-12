package cn.iocoder.yudao.module.datastudio.api.job.dto;

import cn.iocoder.yudao.module.datastudio.api.enums.JobStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataJobUpdateDto {
  private String jobId;

  private JobStatus status;
}
