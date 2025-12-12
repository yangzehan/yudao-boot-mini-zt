package cn.iocoder.yudao.module.flink.deploy.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.flink.client.program.ClusterClient;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClusterMonitorDto {
  private ClusterClient<?> clusterClient;

  private List<String> jobIds;
}
