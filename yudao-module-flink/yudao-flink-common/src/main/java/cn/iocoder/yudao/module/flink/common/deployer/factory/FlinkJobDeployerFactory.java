package cn.iocoder.yudao.module.flink.common.deployer.factory;

import cn.iocoder.yudao.module.flink.common.deployer.FlinkJobDeployer;

/**
 * @author yzh
 */
public interface FlinkJobDeployerFactory {
    FlinkJobDeployer getDeployerFactoryByDeployMode(String deployMode);
}
