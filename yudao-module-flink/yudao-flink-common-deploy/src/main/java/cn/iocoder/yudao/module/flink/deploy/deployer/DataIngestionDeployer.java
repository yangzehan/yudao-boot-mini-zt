package cn.iocoder.yudao.module.flink.deploy.deployer;

import cn.iocoder.yudao.module.flink.common.dto.JobDeployRespDto;
import cn.iocoder.yudao.module.flink.deploy.param.DeployLocalDataIngestionParam;
import cn.iocoder.yudao.module.flink.deploy.param.DeployRemoteDataIngestionParam;
import cn.iocoder.yudao.module.flink.deploy.param.DeployYarnDataIngestionParam;

/**
 * 数据摄取部署器接口
 *
 * <p>负责将数据摄取作业部署到不同的运行环境：本地、远程 Flink 集群、YARN</p>
 *
 * @author yzh
 */
public interface DataIngestionDeployer {

    /**
     * 部署本地数据摄取作业
     *
     * @param param 本地部署参数
     * @return 部署结果
     */
    JobDeployRespDto deployLocal(DeployLocalDataIngestionParam param);

    /**
     * 部署远程数据摄取作业
     *
     * @param param 远程部署参数
     * @return 部署结果
     */
    JobDeployRespDto deployRemote(DeployRemoteDataIngestionParam param);

    /**
     * 部署 YARN 数据摄取作业
     *
     * @param param YARN 部署参数
     * @return 部署结果
     */
    JobDeployRespDto deployYarn(DeployYarnDataIngestionParam param);

}
