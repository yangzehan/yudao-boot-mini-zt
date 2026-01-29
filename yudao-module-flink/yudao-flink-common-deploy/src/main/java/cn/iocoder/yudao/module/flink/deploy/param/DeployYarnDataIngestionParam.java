package cn.iocoder.yudao.module.flink.deploy.param;

import cn.iocoder.yudao.module.flink.common.deployer.DeployParam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.flink.configuration.Configuration;
import org.springframework.lang.NonNull;

import java.util.List;

/**
 * Yarn 数据摄入部署参数
 *
 * @author yzh
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DeployYarnDataIngestionParam implements DeployParam {
  private Configuration configuration;

  private String content;
  private String jobName;
  private Long clusterId;
  private String clusterName;
  private Long fileId;
  private String cdcDistJarPath;

  /** 远程 Flink lib 目录列表（HDFS 路径，如 hdfs://namenode:port/flink/lib） */
  private List<String> remoteLibDirs;

  @NonNull private String yarnSitePath;
  @NonNull private String hdfsSitePath;
  @NonNull private String coreSitePath;
}
