package cn.iocoder.yudao.module.flink.deploy.factory;

import static org.apache.flink.streaming.api.environment.ExecutionCheckpointingOptions.CHECKPOINTING_INTERVAL;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.flink.common.deployer.DeployParam;
import cn.iocoder.yudao.module.flink.common.dto.FlinkConfig;
import cn.iocoder.yudao.module.flink.common.dto.JobDeployDataIngestionReqDto;
import cn.iocoder.yudao.module.flink.common.dto.JobDeployJarReqDto;
import cn.iocoder.yudao.module.flink.common.dto.JobDeploySqlReqDto;
import cn.iocoder.yudao.module.flink.deploy.enums.DeployModeEnum;
import cn.iocoder.yudao.module.flink.deploy.param.*;
import java.util.Iterator;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.CoreOptions;
import org.apache.flink.configuration.DeploymentOptions;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.util.NetUtils;

/**
 * Flink执行参数工厂
 *
 * <p>用于创建不同执行模式下的Flink作业执行参数，包括本地执行参数和远程执行参数。 使用静态工厂方法模式，避免单例模式的复杂性。
 *
 * @author yzh
 */
public final class ExecuteParamFactory {

  /** 私有构造器，防止实例化 */
  private ExecuteParamFactory() {
    throw new UnsupportedOperationException("工具类不允许实例化");
  }

  /**
   * 根据SQL请求DTO创建执行参数
   *
   * @param reqDto SQL请求DTO
   * @return 执行参数（本地或远程）
   */
  public static DeployParam createByJobSubmitSqlReqDto(JobDeploySqlReqDto reqDto) {
    // 添加空指针检查
    Assert.notNull(reqDto, "请求参数不能为空");
    Assert.notNull(reqDto.getFlinkConfig(), "Flink配置不能为空");
    Configuration configuration = parseFlinkConfig(reqDto.getFlinkConfig());
    DeployModeEnum deployModeEnum =
        EnumUtil.getBy(DeployModeEnum::getDeployName, reqDto.getFlinkConfig().getDeployMode());
    Assert.notNull(deployModeEnum, "无法接受该执行类型{}", reqDto.getFlinkConfig().getDeployMode());

    switch (deployModeEnum) {
      case LOCAL:
        DeployLocalSqlParam localParam = new DeployLocalSqlParam();
        localParam.setSql(reqDto.getSql());
        localParam.setConfiguration(configuration);
        localParam.setJobName(reqDto.getJobName());
        return localParam;
      case REMOTE:
        DeployRemoteSqlParam remoteParam = new DeployRemoteSqlParam();
        remoteParam.setSql(reqDto.getSql());
        remoteParam.setConfiguration(configuration);
        remoteParam.setJobName(reqDto.getJobName());
        return remoteParam;
      case YARN_APPLICATION:
        if (!ObjUtil.isAllNotEmpty(
            reqDto.getFlinkConfig().getYarnSitePath(),
            reqDto.getFlinkConfig().getHdfsSitePath(),
            reqDto.getFlinkConfig().getCoreSitePath())) {
          throw new IllegalArgumentException("请检查YarnSitePath、HdfsSitePath、CoreSitePath是否填写");
        }
        DeployYarnSqlParam yarnSqlParam = new DeployYarnSqlParam();
        yarnSqlParam.setSql(reqDto.getSql());
        yarnSqlParam.setConfiguration(configuration);
        yarnSqlParam.setJobName(reqDto.getJobName());
        yarnSqlParam.setYarnSitePath(reqDto.getFlinkConfig().getYarnSitePath());
        yarnSqlParam.setHdfsSitePath(reqDto.getFlinkConfig().getHdfsSitePath());
        yarnSqlParam.setCoreSitePath(reqDto.getFlinkConfig().getCoreSitePath());
        return yarnSqlParam;
      default:
        // 添加默认处理，防止意外情况
        throw new IllegalArgumentException("不支持的执行模式: " + deployModeEnum);
    }
  }

  /**
   * 根据JAR请求DTO创建执行参数
   *
   * @param request JAR请求DTO
   * @return 执行参数（本地或远程）
   */
  public static DeployParam createByJobSubmitJarReqDto(JobDeployJarReqDto request) {
    // 添加空指针检查
    Assert.notNull(request, "请求参数不能为空");
    Assert.notNull(request.getFlinkConfig(), "Flink配置不能为空");
    Configuration configuration = parseFlinkConfig(request.getFlinkConfig());
    DeployModeEnum deployModeEnum =
        EnumUtil.getBy(DeployModeEnum::getDeployName, request.getFlinkConfig().getDeployMode());
    Assert.notNull(deployModeEnum, "无法接受该执行类型{}", request.getFlinkConfig().getDeployMode());

    switch (deployModeEnum) {
      case LOCAL:
        DeployLocalJarParam localParam = new DeployLocalJarParam();
        localParam.setJarFile(request.getJarFile());
        localParam.setEntryPointClassName(request.getEntryPointClassName());
        localParam.setArgument(request.getArgs());
        localParam.setConfiguration(configuration);
        localParam.setJobName(request.getJobName());
        return localParam;
      case REMOTE:
        DeployRemoteJarParam remoteParam = new DeployRemoteJarParam();
        remoteParam.setJarFile(request.getJarFile());
        remoteParam.setEntryPointClassName(request.getEntryPointClassName());
        remoteParam.setArgument(request.getArgs());
        remoteParam.setConfiguration(configuration);
        remoteParam.setJobName(request.getJobName());
        return remoteParam;
      case YARN_APPLICATION:
        if (!ObjUtil.isAllNotEmpty(
            request.getFlinkConfig().getYarnSitePath(),
            request.getFlinkConfig().getHdfsSitePath(),
            request.getFlinkConfig().getCoreSitePath())) {
          throw new IllegalArgumentException("请检查YarnSitePath、HdfsSitePath、CoreSitePath是否填写");
        }
        DeployYarnJarParam yarnJarParam = new DeployYarnJarParam();
        yarnJarParam.setJarFile(request.getJarFile());
        yarnJarParam.setEntryPointClassName(request.getEntryPointClassName());
        yarnJarParam.setArgument(request.getArgs());
        yarnJarParam.setJobName(request.getJobName());
        yarnJarParam.setConfiguration(configuration);
        yarnJarParam.setYarnSitePath(request.getFlinkConfig().getYarnSitePath());
        yarnJarParam.setHdfsSitePath(request.getFlinkConfig().getHdfsSitePath());
        yarnJarParam.setCoreSitePath(request.getFlinkConfig().getCoreSitePath());
        return yarnJarParam;
      default:
        throw new IllegalArgumentException("不支持的执行模式: " + deployModeEnum);
    }
  }

  /**
   * 解析Flink配置
   *
   * @param flinkConfig Flink配置
   * @return 解析后的Configuration对象
   */
  private static Configuration parseFlinkConfig(FlinkConfig flinkConfig) {
    // 构建基础配置
    Configuration configuration = new Configuration();
    configuration.addAll(Configuration.fromMap(flinkConfig.getExtendedConfig()));

    Configuration effectiveConfiguration = Configuration.fromMap(flinkConfig.getExtendedConfig());
    effectiveConfiguration.setString(DeploymentOptions.TARGET, flinkConfig.getDeployMode());
    effectiveConfiguration.setBoolean(DeploymentOptions.ATTACHED, true);

    // 添加检查点配置
    if (flinkConfig.getCheckpointInterval() != null) {
      // execution.checkpointing.interval - 检查点间隔
      effectiveConfiguration.set(
          CHECKPOINTING_INTERVAL, java.time.Duration.ofMillis(flinkConfig.getCheckpointInterval()));
    }

    // 添加检查点存储路径配置（如果填写了的话）
    if (flinkConfig.getCheckpointPath() != null && !flinkConfig.getCheckpointPath().isEmpty()) {
      // state.checkpoints.checkpoint-storage - 检查点存储路径
      effectiveConfiguration.set(
          org.apache.flink.configuration.CheckpointingOptions.CHECKPOINTS_DIRECTORY,
          flinkConfig.getCheckpointPath());
    }

    // 添加 parallelism 配置
    if (flinkConfig.getParallelism() != null) {
      effectiveConfiguration.set(CoreOptions.DEFAULT_PARALLELISM, flinkConfig.getParallelism());
    }

    String restBindPort = flinkConfig.getExtendedConfig().get("rest.bind-port");

    if (StrUtil.isNotEmpty(restBindPort)) {
      Iterator<Integer> range = NetUtils.getPortRangeFromString(restBindPort);
      while (range.hasNext()) {
        Integer port = range.next();
        if (NetUtil.isUsableLocalPort(port)) {
          effectiveConfiguration.setString("rest.port", String.valueOf(port));
          effectiveConfiguration.removeConfig(RestOptions.BIND_PORT);
          break;
        }
      }
    }

    return effectiveConfiguration;
  }

  public static DeployParam createByJobSubmitDataIngestionReqDto(
      JobDeployDataIngestionReqDto request) {
    // 添加空指针检查
    Assert.notNull(request, "请求参数不能为空");
    Assert.notNull(request.getFlinkConfig(), "Flink配置不能为空");
    Configuration configuration = parseFlinkConfig(request.getFlinkConfig());
    DeployModeEnum deployModeEnum =
        EnumUtil.getBy(DeployModeEnum::getDeployName, request.getFlinkConfig().getDeployMode());
    Assert.notNull(deployModeEnum, "无法接受该执行类型{}", request.getFlinkConfig().getDeployMode());

    switch (deployModeEnum) {
      case LOCAL:
        DeployLocalDataIngestionParam localParam = new DeployLocalDataIngestionParam();
        localParam.setContent(request.getContent());
        localParam.setConfiguration(configuration);
        localParam.setJobName(request.getJobName());
        localParam.setClusterId(request.getClusterId());
        localParam.setClusterName(request.getClusterName());
        localParam.setFileId(request.getFileId());
        return localParam;
      case REMOTE:
        DeployRemoteDataIngestionParam remoteParam = new DeployRemoteDataIngestionParam();
        remoteParam.setContent(request.getContent());
        remoteParam.setConfiguration(configuration);
        remoteParam.setJobName(request.getJobName());
        remoteParam.setClusterId(request.getClusterId());
        remoteParam.setClusterName(request.getClusterName());
        remoteParam.setFileId(request.getFileId());
        return remoteParam;
      case YARN_APPLICATION:
        if (!ObjUtil.isAllNotEmpty(
            request.getFlinkConfig().getYarnSitePath(),
            request.getFlinkConfig().getHdfsSitePath(),
            request.getFlinkConfig().getCoreSitePath())) {
          throw new IllegalArgumentException("请检查YarnSitePath、HdfsSitePath、CoreSitePath是否填写");
        }
        // 验证cdcDistJarPath必填
        if (!ObjUtil.isNotEmpty(request.getFlinkCdcDistJarPath())) {
          throw new IllegalArgumentException("请填写flinkCdcDistJarPath");
        }
        DeployYarnDataIngestionParam yarnParam = new DeployYarnDataIngestionParam();
        yarnParam.setContent(request.getContent());
        yarnParam.setConfiguration(configuration);
        yarnParam.setJobName(request.getJobName());
        yarnParam.setClusterId(request.getClusterId());
        yarnParam.setClusterName(request.getClusterName());
        yarnParam.setFileId(request.getFileId());
        yarnParam.setCdcDistJarPath(request.getFlinkCdcDistJarPath());
        yarnParam.setYarnSitePath(request.getFlinkConfig().getYarnSitePath());
        yarnParam.setHdfsSitePath(request.getFlinkConfig().getHdfsSitePath());
        yarnParam.setCoreSitePath(request.getFlinkConfig().getCoreSitePath());
        return yarnParam;
      default:
        throw new IllegalArgumentException("不支持的执行模式: " + deployModeEnum);
    }
  }
}
