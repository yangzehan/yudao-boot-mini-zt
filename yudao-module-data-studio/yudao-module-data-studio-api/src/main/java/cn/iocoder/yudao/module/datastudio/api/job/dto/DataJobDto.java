package cn.iocoder.yudao.module.datastudio.api.job.dto;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * @author yzh
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataJobDto {
  @NonNull private Map<String, String> config;
  @NonNull private String jobId;
  @NonNull private String webUiUrl;
  @Nullable private String flinkClusterId;
}
