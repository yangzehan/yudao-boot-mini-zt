package cn.iocoder.yudao.module.datastudio.service.job.impl;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.datastudio.controller.admin.job.vo.JobDeployReqVO;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.flinkcluster.FlinkClusterDO;
import cn.iocoder.yudao.module.datastudio.framework.flink.client.FlinkApiFactory;
import cn.iocoder.yudao.module.datastudio.service.flinkcluster.FlinkClusterService;
import cn.iocoder.yudao.module.datastudio.service.job.FlinkJarService;
import cn.iocoder.yudao.module.flink.common.api.FlinkApi;
import cn.iocoder.yudao.module.flink.common.dal.dataobject.FlinkJobDeployDO;
import cn.iocoder.yudao.module.flink.common.dal.mysql.FlinkJobDeployInfoMapper;
import cn.iocoder.yudao.module.flink.common.dto.FlinkConfig;
import cn.iocoder.yudao.module.flink.common.dto.JobDeployJarReqDto;
import cn.iocoder.yudao.module.flink.common.dto.JobDeployRespDto;
import java.util.HashMap;
import javax.annotation.Resource;

import cn.iocoder.yudao.module.flink.common.enums.JobTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author yzh
 */
@Service
public class FlinkJarServiceImpl implements FlinkJarService {
  @Resource private FlinkClusterService flinkClusterService;
  @Autowired private FlinkJobDeployInfoMapper flinkJobDeployInfoMapper;

  @Override
  public String deploy(JobDeployReqVO reqVO) {
    String flinkVersion;
    JobDeployJarReqDto reqDto = new JobDeployJarReqDto();
    reqDto.setArgs(reqVO.getArguments());
    reqDto.setJobName(reqVO.getJobName());
    reqDto.setEntryPointClassName(
        StrUtil.isBlank(reqVO.getEntryPointClassName()) ? null : reqVO.getEntryPointClassName());
    reqDto.setJarFile(reqVO.getJarFile());
    FlinkConfig flinkConfig = new FlinkConfig();
    String deployMode = reqVO.getDeployMode();
    if ("remote".equals(deployMode)) {
      FlinkClusterDO flinkCluster = flinkClusterService.getFlinkCluster(reqVO.getClusterId());
      String[] s = flinkCluster.getRemoteUrl().split(":");
      HashMap<String, String> exConfig = new HashMap<>();
      exConfig.put("rest.address", s[0]);
      exConfig.put("rest.port", s[1]);
      flinkConfig.setExtendedConfig(exConfig);
      flinkVersion = flinkCluster.getFlinkVersion();
    } else {
      flinkVersion = reqVO.getFlinkVersion();
    }
    flinkConfig.setDeployMode(deployMode);
    flinkConfig.setClusterId(reqVO.getClusterId());
    flinkConfig.setFlinkVersion(reqVO.getFlinkVersion());
    flinkConfig.setParallelism(reqVO.getParallelism());
    flinkConfig.setCheckpointInterval(reqVO.getCheckpointInterval());
    reqDto.setFlinkConfig(flinkConfig);
    FlinkApi flinkApi = FlinkApiFactory.getFlinkApiByVersion(flinkVersion);
    JobDeployRespDto respDto = flinkApi.deployJar(reqDto).getCheckedData();

    FlinkJobDeployDO flinkJobDeployDO =
        FlinkJobDeployDO.builder()
            .flinkVersion(flinkVersion)
            .jobId(respDto.getJobId())
            .deployMode(deployMode)
            .jobName(reqDto.getJobName())
            .config(respDto.getConfig())
            .submitTime(respDto.getSubmitTime())
            .webUiUrl(respDto.getWebInterfaceUrl())
            .executionMode("")
            .status("running")
            .deployMode(deployMode)
                .jobType(JobTypeEnum.JAR)
            .build();
    flinkJobDeployInfoMapper.insert(flinkJobDeployDO);
    return respDto.getJobId();
  }
}
