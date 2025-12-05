package cn.iocoder.yudao.module.flink.common.deployer;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.module.flink.common.dto.JobDeployRespDto;

import java.util.Map;

/**
 * @author yzh
 */
public interface FlinkJobDeployer {

    JobDeployRespDto deploySql(DeployParam deployParam);

    default DeployParam validSupportDeploySqlParam(DeployParam deployParam) {
        throw ServiceExceptionUtil.exception(new ErrorCode(9999, "不支持此类型执行参数{}"), deployParam.getClass().getName());
    }

    //todo可以抽象模板传入一个class类来强转 如果转不了报错
    DeployParam validSupportDeployJarParam(DeployParam deployParam);

    /**
     * 执行JAR作业
     *
     * @param deployParam 部署参数
     * @return JobDeployRespDto 包含作业ID、集群信息、Web界面URL等
     */
    JobDeployRespDto deployJar(DeployParam deployParam);

    void cancelJob(String jobId, Map<String, String> config);
}
