package cn.iocoder.yudao.module.flink.deploy.enums;

import cn.hutool.core.util.ReflectUtil;
import cn.iocoder.yudao.module.flink.common.deployer.FlinkJobDeployer;
import cn.iocoder.yudao.module.flink.deploy.deployer.FlinkJobLocalDeployerImpl;
import cn.iocoder.yudao.module.flink.deploy.deployer.FlinkJobRemoteDeployerImpl;
import cn.iocoder.yudao.module.flink.deploy.deployer.FlinkYarnJobDyployImpl;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;

/**
 * Flink 部署模式枚举
 *
 * <p>支持动态注册 YARN_APPLICATION 模式的部署器，以便不同 Flink 版本可以使用各自的实现。
 *
 * @author yzh
 */
@Getter
public enum DeployModeEnum {
  REMOTE("remote", FlinkJobRemoteDeployerImpl.class),
  LOCAL("local", FlinkJobLocalDeployerImpl.class),
  // YARN_APPLICATION 不再硬编码，改为动态注册
  YARN_APPLICATION("yarn-application", FlinkYarnJobDyployImpl.class),
  ;

  private static final Map<String, Class<? extends FlinkJobDeployer>> DYNAMIC_DEPLOYERS =
      new ConcurrentHashMap<>();
  private static final Map<String, FlinkJobDeployer> DEPLOYER_INSTANCES = new ConcurrentHashMap<>();

  static {
    // 加载 SPI 注册的部署器
    loadSpiDeployers();
  }

  /** 部署模式名称 */
  private final String deployName;

  /** 部署器类（对于 YARN_APPLICATION 可能为 null，需要从动态注册表查找） */
  private final Class<? extends FlinkJobDeployer> flinkJobDeployClass;

  DeployModeEnum(String deployName, Class<? extends FlinkJobDeployer> flinkJobDeployClass) {
    this.deployName = deployName;
    this.flinkJobDeployClass = flinkJobDeployClass;
  }

  /**
   * 动态注册 YARN 应用程序部署器
   *
   * @param deployMode 部署模式名称（如 "yarn-application"）
   * @param deployerClass 部署器实现类
   */
  public static void registerApplicationDeployer(
      String deployMode, Class<? extends FlinkJobDeployer> deployerClass) {
    DYNAMIC_DEPLOYERS.put(deployMode, deployerClass);
    // 清除缓存的实例，以便下次获取时使用新的类
    DEPLOYER_INSTANCES.remove(deployMode);
  }

  /**
   * 获取部署器实例
   *
   * @param deployMode 部署模式名称
   * @return 部署器实例
   */
  public static FlinkJobDeployer getDeployer(String deployMode) {
    // 首先检查是否有缓存的实例
    FlinkJobDeployer cachedInstance = DEPLOYER_INSTANCES.get(deployMode);
    if (cachedInstance != null) {
      return cachedInstance;
    }

    // 从枚举中查找
    DeployModeEnum modeEnum = getByDeployName(deployMode);
    if (modeEnum == null) {
      // 尝试从动态注册表查找
      Class<? extends FlinkJobDeployer> deployerClass = DYNAMIC_DEPLOYERS.get(deployMode);
      if (deployerClass != null) {
        FlinkJobDeployer instance = ReflectUtil.newInstance(deployerClass);
        DEPLOYER_INSTANCES.put(deployMode, instance);
        return instance;
      }
      throw new IllegalArgumentException("无法找到部署模式: " + deployMode);
    }

    // 使用枚举中定义的类
    Class<? extends FlinkJobDeployer> deployerClass = modeEnum.getFlinkJobDeployClass();
    if (deployerClass == null) {
      // 尝试从动态注册表查找
      deployerClass = DYNAMIC_DEPLOYERS.get(deployMode);
      if (deployerClass == null) {
        throw new IllegalArgumentException("部署模式 " + deployMode + " 未注册部署器类");
      }
    }

    FlinkJobDeployer instance = ReflectUtil.newInstance(deployerClass);
    DEPLOYER_INSTANCES.put(deployMode, instance);
    return instance;
  }

  /**
   * 根据部署模式名称获取枚举
   *
   * @param deployName 部署模式名称
   * @return 枚举实例，如果未找到则返回 null
   */
  public static DeployModeEnum getByDeployName(String deployName) {
    for (DeployModeEnum mode : values()) {
      if (mode.getDeployName().equals(deployName)) {
        return mode;
      }
    }
    return null;
  }

  /**
   * 检查部署模式是否已注册
   *
   * @param deployMode 部署模式名称
   * @return 是否已注册
   */
  public static boolean isRegistered(String deployMode) {
    DeployModeEnum mode = getByDeployName(deployMode);
    if (mode != null && mode.flinkJobDeployClass != null) {
      return true;
    }
    return DYNAMIC_DEPLOYERS.containsKey(deployMode);
  }

  /** 加载 SPI 注册的部署器 */
  private static void loadSpiDeployers() {
    try {
      ServiceLoader<FlinkJobDeployer> serviceLoader = ServiceLoader.load(FlinkJobDeployer.class);
      for (FlinkJobDeployer deployer : serviceLoader) {
        // 通过注解或接口获取部署模式
        // 这里简化处理，实际可以通过 @Order 注解指定优先级
      }
    } catch (Exception e) {
      // SPI 加载失败不影响正常启动
    }
  }
}
