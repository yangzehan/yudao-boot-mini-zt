package cn.iocoder.yudao.module.datastudio.api.job;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.datastudio.api.enums.JobStatus;
import cn.iocoder.yudao.module.datastudio.api.job.dto.DataJobDto;
import cn.iocoder.yudao.module.datastudio.api.job.dto.DataJobUpdateDto;
import cn.iocoder.yudao.module.datastudio.dal.mysql.job.DataJobMapper;
import cn.iocoder.yudao.module.flink.common.dal.dataobject.FlinkJobDeployDO;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yzh
 */
@RestController
@Validated
@Slf4j
public class DataJobApiImpl implements DataJobApi {
  @Resource private DataJobMapper dataJobMapper;
  @Resource private RedisTemplate<Object, Object> redisTemplate;

  @Override
  @TenantIgnore
  public CommonResult<Boolean> updateJob(DataJobUpdateDto dto) {
    FlinkJobDeployDO entity = new FlinkJobDeployDO();
    entity.setJobId(dto.getJobId());
    entity.setStatus(JobStatus.valueOf(dto.getStatus()));
    LambdaQueryWrapperX<FlinkJobDeployDO> wrapper = new LambdaQueryWrapperX<>();
    wrapper.eq(FlinkJobDeployDO::getJobId, dto.getJobId());
    dataJobMapper.update(entity, wrapper);
    return CommonResult.success(true);
  }

  @Override
  @TenantIgnore
  public CommonResult<Boolean> updateJobBatch(List<DataJobUpdateDto> dtos) {
    ArrayList<FlinkJobDeployDO> jobDeployDOS = new ArrayList<>();

    for (DataJobUpdateDto dto : dtos) {
      FlinkJobDeployDO entity = new FlinkJobDeployDO();
      entity.setJobId(dto.getJobId());
      entity.setStatus(JobStatus.valueOf(dto.getStatus()));
      jobDeployDOS.add(entity);
    }

    dataJobMapper.updateBatch(jobDeployDOS);
    return CommonResult.success(true);
  }

  @Override
  @TenantIgnore
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

  @Override
  @TenantIgnore
  public CommonResult<List<DataJobDto>> listJob(JobStatus jobStatus, String flinkVersion) {
    LambdaQueryWrapperX<FlinkJobDeployDO> wrapper = new LambdaQueryWrapperX<>();
    wrapper.eqIfPresent(FlinkJobDeployDO::getStatus, jobStatus);
    wrapper.eqIfPresent(FlinkJobDeployDO::getFlinkVersion, flinkVersion);

    List<FlinkJobDeployDO> dos = dataJobMapper.selectList(wrapper);
    if (CollectionUtil.isEmpty(dos)) {
      return CommonResult.success(Collections.emptyList());
    }
    List<DataJobDto> dataJobDtos = new ArrayList<>();
    for (FlinkJobDeployDO deployDO : dos) {
      DataJobDto dataJobDto =
          new DataJobDto(
              deployDO.getConfig(),
              deployDO.getJobId(),
              deployDO.getWebUiUrl(),
              deployDO.getFlinkClusterId());
      dataJobDtos.add(dataJobDto);
    }
    return CommonResult.success(dataJobDtos);
  }

  @Override
  @TenantIgnore
  public CommonResult<Boolean> shouldMonitor(String jobId) {
    // 获得该jobId所对应的缓存
    if (ObjUtil.isNull(redisTemplate.opsForValue().get(jobId))) {
      redisTemplate.opsForValue().set(jobId, "1");
      return CommonResult.success(true);
    }
    return CommonResult.success(false);
  }

  @Override
  @TenantIgnore
  public CommonResult<Boolean> monitorJobFinished(String jobId, JobStatus jobStatus) {
    redisTemplate.opsForValue().getAndDelete("jobId");

    if (jobStatus.equals(JobStatus.RUNNING)) {
      log.info("作业{}还在运行无需修改状态", jobId);
    }
    // 先查一下作业状态，如果jobStatusHook已经处理了作业状态则无需更新
    FlinkJobDeployDO job = dataJobMapper.selectOne(FlinkJobDeployDO::getJobId, jobId);
    if (job == null && !job.getStatus().equals(JobStatus.RUNNING)) {
      log.info("作业{}已经不再运行，无需更新作业状态", jobId);
      return CommonResult.success(true);
    }
    LambdaQueryWrapperX<FlinkJobDeployDO> w = new LambdaQueryWrapperX<>();
    w.eq(FlinkJobDeployDO::getJobId, jobId);
    FlinkJobDeployDO deployDO = new FlinkJobDeployDO();
    deployDO.setStatus(jobStatus);
    dataJobMapper.update(deployDO, w);
    log.info("作业{}状态更新成功，当前状态{}", jobId, jobStatus);
    return CommonResult.success(true);
  }
}
