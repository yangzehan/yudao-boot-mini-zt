package cn.iocoder.yudao.module.flink.deploy.param;

import cn.iocoder.yudao.module.flink.common.deployer.DeployParam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.flink.configuration.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * @author yzh
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DeployYarnJarParam implements DeployParam {
  @Nullable private Configuration configuration;
  private String jarFile;

  /** 远程 Flink lib 目录列表（HDFS 路径，如 hdfs://namenode:port/flink/lib） */
  private java.util.List<String> remoteLibDirs;

  private String entryPointClassName;
  private String[] argument;
  private String jobName;
  @NonNull private String yarnSitePath;
  @NonNull private String hdfsSitePath;
  @NonNull private String coreSitePath;
}
