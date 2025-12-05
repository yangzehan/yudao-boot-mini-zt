package cn.iocoder.yudao.module.flink.deploy.enums;

import cn.iocoder.yudao.module.flink.common.deployer.FlinkJobDeployer;
import cn.iocoder.yudao.module.flink.deploy.FlinkJobLocalDeployerImpl;
import cn.iocoder.yudao.module.flink.deploy.FlinkJobRemoteDeployerImpl;
import lombok.Getter;


/**
 * @author yzh
 */

@Getter
public enum DeployModeEnum {


    REMOTE("remote", FlinkJobRemoteDeployerImpl.class),
    LOCAL("local", FlinkJobLocalDeployerImpl.class);

    private final Class<? extends FlinkJobDeployer> flinkJobDeployClass;
    private final String deployName;

    DeployModeEnum(String deployName, Class<? extends FlinkJobDeployer> flinkJobDeployClass) {
        this.deployName = deployName;
        this.flinkJobDeployClass = flinkJobDeployClass;
    }

}
