package cn.iocoder.yudao.module.flink.deploy.factory;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.EnumUtil;
import cn.iocoder.yudao.module.flink.common.deployer.DeployParam;
import cn.iocoder.yudao.module.flink.common.dto.FlinkConfig;
import cn.iocoder.yudao.module.flink.common.dto.JobDeployJarReqDto;
import cn.iocoder.yudao.module.flink.common.dto.JobDeploySqlReqDto;
import cn.iocoder.yudao.module.flink.deploy.enums.DeployModeEnum;
import cn.iocoder.yudao.module.flink.deploy.param.DeployLocalJarParam;
import cn.iocoder.yudao.module.flink.deploy.param.DeployLocalSqlParam;
import cn.iocoder.yudao.module.flink.deploy.param.DeployRemoteJarParam;
import cn.iocoder.yudao.module.flink.deploy.param.DeployRemoteSqlParam;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.DeploymentOptions;

/**
 * Flink执行参数工厂
 * <p>
 * 用于创建不同执行模式下的Flink作业执行参数，包括本地执行参数和远程执行参数。
 * 使用静态工厂方法模式，避免单例模式的复杂性。
 *
 * @author yzh
 */
public final class ExecuteParamFactory {

    /**
     * 私有构造器，防止实例化
     */
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
        DeployModeEnum deployModeEnum = EnumUtil.getBy(DeployModeEnum::getDeployName, reqDto.getFlinkConfig().getDeployMode());
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
        Configuration configuration = parseFlinkConfig(request.getFlinkConfig());

        switch (request.getFlinkConfig().getDeployMode()) {
            case "local":
                DeployLocalJarParam localParam = new DeployLocalJarParam();
                localParam.setJarFile(request.getJarFile());
                localParam.setEntryPointClassName(request.getEntryPointClassName());
                localParam.setArgument(request.getArgs());
                localParam.setConfiguration(configuration);
                localParam.setJobName(request.getJobName());
                return localParam;
            case "remote":
                DeployRemoteJarParam remoteParam = new DeployRemoteJarParam();
                remoteParam.setJarFile(request.getJarFile());
                remoteParam.setEntryPointClassName(request.getEntryPointClassName());
                remoteParam.setArgument(request.getArgs());
                remoteParam.setConfiguration(configuration);
                remoteParam.setJobName(request.getJobName());
                return remoteParam;
            default:
                throw new IllegalArgumentException("不支持的执行模式: " + request.getFlinkConfig().getDeployMode());


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
        return effectiveConfiguration;
    }
}
