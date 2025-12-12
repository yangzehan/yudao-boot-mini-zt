package cn.iocoder.yudao.module.datastudio.api.job;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.datastudio.api.enums.JobStatus;
import cn.iocoder.yudao.module.datastudio.api.job.dto.DataJobUpdateDto;
import cn.iocoder.yudao.module.datastudio.dal.mysql.job.DataJobMapper;
import cn.iocoder.yudao.module.flink.common.dal.dataobject.FlinkJobDeployDO;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
public class DataJobApiImpl implements DataJobApi {
  @Resource private DataJobMapper dataJobMapper;

  @Override
  public CommonResult<Boolean> updateJob(DataJobUpdateDto dto) {
    FlinkJobDeployDO entity = new FlinkJobDeployDO();
    entity.setJobId(dto.getJobId());
    entity.setStatus(dto.getStatus());
    LambdaQueryWrapperX<FlinkJobDeployDO> wrapper = new LambdaQueryWrapperX<>();
    wrapper.eq(FlinkJobDeployDO::getJobId, dto.getJobId());
    dataJobMapper.update(entity, wrapper);
    return CommonResult.success(true);
  }

  @Override
  public CommonResult<Boolean> updateJobBatch(List<DataJobUpdateDto> dtos) {
    ArrayList<FlinkJobDeployDO> jobDeployDOS = new ArrayList<>();

    for (DataJobUpdateDto dto : dtos) {
      FlinkJobDeployDO entity = new FlinkJobDeployDO();
      entity.setJobId(dto.getJobId());
      entity.setStatus(dto.getStatus());
      jobDeployDOS.add(entity);
    }

    dataJobMapper.updateBatch(jobDeployDOS);
    return CommonResult.success(true);
  }

  @Override
  public CommonResult<Boolean> updateJob(String webInterfaceURL) {

    LambdaQueryWrapperX<FlinkJobDeployDO> queryWrapper = new LambdaQueryWrapperX<>();
    FlinkJobDeployDO entity = new FlinkJobDeployDO();
    queryWrapper
        .notIn(
            FlinkJobDeployDO::getStatus, JobStatus.CANCELED, JobStatus.FAILED, JobStatus.FINISHED)
        .eq(FlinkJobDeployDO::getWebUiUrl, webInterfaceURL);
    entity.setStatus(JobStatus.ClOSED);
    dataJobMapper.update(entity, queryWrapper);

    return CommonResult.success(true);
  }
}
