package cn.iocoder.yudao.module.datastudio.api.enums;

import lombok.Getter;

/**
 * @author yzh
 */
@Getter
public enum FlinkVersion {
  FLINK_1_18("1.18"),
  FLINK_1_19("1.19"),
  ;

  private final String version;

  FlinkVersion(String version) {
    this.version = version;
  }

  /**
   * 根据版本字符串获取对应的枚举值
   *
   * @param versionStr 版本字符串，如 "1.18", "1.19"
   * @return 对应的枚举值
   * @throws IllegalArgumentException 如果找不到匹配的版本
   */
  public static FlinkVersion fromVersion(String versionStr) {
    for (FlinkVersion version : FlinkVersion.values()) {
      if (version.version.equals(versionStr)) {
        return version;
      }
    }
    throw new IllegalArgumentException("No enum constant cn.iocoder.yudao.module.datastudio.api.enums.FlinkVersion." + versionStr);
  }
}
