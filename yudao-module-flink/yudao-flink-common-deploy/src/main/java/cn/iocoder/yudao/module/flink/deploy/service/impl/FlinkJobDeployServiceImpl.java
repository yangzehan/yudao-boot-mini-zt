package cn.iocoder.yudao.module.flink.deploy.service.impl;

import cn.iocoder.yudao.module.flink.common.deployer.DeployParam;
import cn.iocoder.yudao.module.flink.common.deployer.FlinkJobDeployer;
import cn.iocoder.yudao.module.flink.common.dto.*;
import cn.iocoder.yudao.module.flink.common.service.FlinkJobDeployService;
import cn.iocoder.yudao.module.flink.deploy.factory.ExecuteParamFactory;
import cn.iocoder.yudao.module.flink.deploy.factory.FlinkJobDeployerFactoryImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.configuration.DeploymentOptions;
import org.springframework.stereotype.Service;

/**
 * Flink 任务执行记录 Service 实现类
 *
 * @author 芋道源码
 */
@Slf4j
@Service
public class FlinkJobDeployServiceImpl implements FlinkJobDeployService {

  @Override
  public JobDeployRespDto deployJob(JobDeployRequest request) {
    return null;
  }

  @Override
  public JobStatusResponse getJobStatus(String jobId) {
    return null;
  }

  @Override
  public void cancelJob(JobCancelReqDto reqDto) {
    FlinkJobDeployer deployer =
        FlinkJobDeployerFactoryImpl.getInstance()
            .getDeployerFactoryByDeployMode(reqDto.getConfig().get(DeploymentOptions.TARGET.key()));
    deployer.cancelJob(reqDto.getJobId(), reqDto.getConfig());
  }

  @Override
  public JobDeployRespDto deploySql(JobDeploySqlReqDto request) {

    // 根据执行模式获取对应的作业执行器
    FlinkJobDeployer deployer =
        FlinkJobDeployerFactoryImpl.getInstance()
            .getDeployerFactoryByDeployMode(request.getFlinkConfig().getDeployMode());
    // 构建执行参数
    DeployParam deployParam = ExecuteParamFactory.createByJobSubmitSqlReqDto(request);
    // 执行作业
    return deployer.deploySql(deployParam);
  }

  @Override
  public JobDeployRespDto deployJar(JobDeployJarReqDto request) {
    // 根据执行模式获取对应的作业执行器
    FlinkJobDeployer deployer =
        FlinkJobDeployerFactoryImpl.getInstance()
            .getDeployerFactoryByDeployMode(request.getFlinkConfig().getDeployMode());
    // 构建执行参数
    DeployParam deployParam = ExecuteParamFactory.createByJobSubmitJarReqDto(request);
    // 执行作业并获取结果
    return deployer.deployJar(deployParam, true);
  }

  @Override
  public JobDeployRespDto deployDataIngestion(JobDeployDataIngestionReqDto request) {
    FlinkJobDeployer deployer =
        FlinkJobDeployerFactoryImpl.getInstance()
            .getDeployerFactoryByDeployMode(request.getFlinkConfig().getDeployMode());
    // 构建执行参数
    DeployParam deployParam = ExecuteParamFactory.createByJobSubmitDataIngestionReqDto(request);
    return deployer.deployDataIngestion(deployParam);
  }
}
