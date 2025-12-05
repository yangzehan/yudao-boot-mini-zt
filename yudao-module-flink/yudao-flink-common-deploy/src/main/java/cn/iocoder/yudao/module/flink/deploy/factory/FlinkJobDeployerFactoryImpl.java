package cn.iocoder.yudao.module.flink.deploy.factory;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.iocoder.yudao.module.flink.common.deployer.FlinkJobDeployer;
import cn.iocoder.yudao.module.flink.common.deployer.factory.FlinkJobDeployerFactory;
import cn.iocoder.yudao.module.flink.deploy.enums.DeployModeEnum;

/**
 * Flink作业执行工厂实现类
 * <p>
 * 使用静态内部类实现线程安全的单例模式
 *
 * @author yzh
 */
public class FlinkJobDeployerFactoryImpl implements FlinkJobDeployerFactory {

    /**
     * 私有构造函数，防止外部实例化
     */
    private FlinkJobDeployerFactoryImpl() {

    }

    /**
     * 获取单例实例
     * 使用静态内部类实现线程安全和高性能
     *
     * @return FlinkJobExecuteFactoryImpl单例实例
     */
    public static FlinkJobDeployerFactoryImpl getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public FlinkJobDeployer getDeployerFactoryByDeployMode(String deployMode) {
        DeployModeEnum deployModeEnum = EnumUtil.getBy(DeployModeEnum::getDeployName, deployMode);
        Assert.notNull(deployModeEnum, "无法接受该执行类型{}", deployMode);
        Class<? extends FlinkJobDeployer> executorClass = deployModeEnum.getFlinkJobDeployClass();
        return ReflectUtil.newInstance(executorClass);
    }

    /**
     * 静态内部类，实现线程安全的延迟初始化单例
     */
    private static class Holder {
        static final FlinkJobDeployerFactoryImpl INSTANCE = new FlinkJobDeployerFactoryImpl();
    }
}
