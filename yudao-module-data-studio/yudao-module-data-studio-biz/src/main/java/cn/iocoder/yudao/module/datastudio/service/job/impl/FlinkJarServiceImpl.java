package cn.iocoder.yudao.module.datastudio.service.job.impl;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.datastudio.api.enums.JobStatus;
import cn.iocoder.yudao.module.datastudio.controller.admin.job.vo.JobDeployReqVO;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.flink.job.deploy.FlinkJobDeployDO;
import cn.iocoder.yudao.module.datastudio.dal.mysql.flink.job.deploy.FlinkJobDeployInfoMapper;
import cn.iocoder.yudao.module.datastudio.framework.flink.client.FlinkApiFactory;
import cn.iocoder.yudao.module.datastudio.service.flinkcluster.FlinkClusterService;
import cn.iocoder.yudao.module.datastudio.service.job.FlinkJarService;
import cn.iocoder.yudao.module.datastudio.util.DeployUtil;
import cn.iocoder.yudao.module.flink.common.api.FlinkApi;
import cn.iocoder.yudao.module.flink.common.dto.FlinkConfig;
import cn.iocoder.yudao.module.flink.common.dto.JobDeployJarReqDto;
import cn.iocoder.yudao.module.flink.common.dto.JobDeployRespDto;
import cn.iocoder.yudao.module.flink.common.enums.JobTypeEnum;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * @author yzh
 */
@Service
public class FlinkJarServiceImpl implements FlinkJarService {
  @Resource private FlinkClusterService flinkClusterService;
  @Resource private FlinkJobDeployInfoMapper flinkJobDeployInfoMapper;
  @Resource private DeployUtil deployUtil;

  @Override
  public String deploy(JobDeployReqVO reqVO) {

    JobDeployJarReqDto reqDto = new JobDeployJarReqDto();
    reqDto.setArgs(reqVO.getArguments());
    reqDto.setJobName(reqVO.getJobName());
    reqDto.setEntryPointClassName(
        StrUtil.isBlank(reqVO.getEntryPointClassName()) ? null : reqVO.getEntryPointClassName());
    reqDto.setJarFile(reqVO.getJarFile());
    // 获取flink配置信息
    FlinkConfig flinkConfig = deployUtil.getFlinkConfigByJobDeployReqVO(reqVO);
    String flinkVersion = flinkConfig.getFlinkVersion();

    reqDto.setFlinkConfig(flinkConfig);
    FlinkApi flinkApi = FlinkApiFactory.getFlinkApiByVersion(flinkVersion);
    JobDeployRespDto respDto = flinkApi.deployJar(reqDto).getCheckedData();

    FlinkJobDeployDO flinkJobDeployDO =
        FlinkJobDeployDO.builder()
            .flinkVersion(flinkVersion)
            .jobId(respDto.getJobId())
            .deployMode(flinkConfig.getDeployMode())
            .jobName(reqDto.getJobName())
            .config(respDto.getConfig())
            .submitTime(respDto.getSubmitTime())
            .webUiUrl(respDto.getWebInterfaceUrl())
            .executionMode("")
            .status(JobStatus.RUNNING)
            .jobType(JobTypeEnum.JAR)
            .flinkClusterId(respDto.getFlinkClusterId())
            .build();
    flinkJobDeployInfoMapper.insert(flinkJobDeployDO);
    return respDto.getJobId();
  }
}
