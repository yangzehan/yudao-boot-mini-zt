package cn.iocoder.yudao.module.flink.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.springframework.lang.Nullable;

/**
 * Flink配置信息 用于存储SQL任务的执行配置，支持扩展
 *
 * @author 芋道源码
 */
@Data
public class FlinkConfig implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 扩展配置项 用于存储额外的配置，支持未来扩展 */
  @JsonProperty("extendedConfig")
  @Nullable
  private Map<String, String> extendedConfig = new HashMap<>();

  /** 执行模式：local-本地模式，remote-远程模式 */
  @JsonProperty("deployMode")
  private String deployMode = "local";

  /** 集群ID */
  @JsonProperty("clusterId")
  private Long clusterId;

  /** Flink版本：1.14, 1.15, 1.16, 1.17, 1.18 */
  @JsonProperty("flinkVersion")
  private String flinkVersion = "1.16";

  /** 并行度：1-1000 */
  @JsonProperty("parallelism")
  private Integer parallelism = 1;

  /** 检查点间隔(毫秒)：1000-3600000 */
  @JsonProperty("checkpointInterval")
  private Long checkpointInterval = 5000L;

  /** 检查点存储路径 - 不填时使用后端默认配置 */
  @JsonProperty("checkpointPath")
  private String checkpointPath;

  /** Flink CDC Dist JAR包路径 - 用于Yarn Application模式下的CDC数据同步 */
  @JsonProperty("flinkCdcDistJarPath")
  private String flinkCdcDistJarPath;

  private String executionType;
  @Nullable private String yarnSitePath;
  @Nullable private String hdfsSitePath;
  @Nullable private String coreSitePath;
  @Nullable private String yarnAppLogConfigPath;

  /** 添加扩展配置项 */
  public void setExtendedConfigValue(String key, String value) {
    if (this.extendedConfig == null) {
      this.extendedConfig = new HashMap<>();
    }
    this.extendedConfig.put(key, value);
  }

  /** 获取扩展配置项 */
  @Nullable
  public Object getExtendedConfigValue(String key) {
    if (this.extendedConfig == null) {
      return null;
    }
    return this.extendedConfig.get(key);
  }

  /** 获取扩展配置项（带默认值） */
  public Object getExtendedConfigValue(String key, String defaultValue) {
    if (this.extendedConfig == null) {
      return defaultValue;
    }
    return this.extendedConfig.getOrDefault(key, defaultValue);
  }
}
