package cn.iocoder.yudao.module.datastudio.service.job.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.datastudio.api.enums.JobStatus;
import cn.iocoder.yudao.module.datastudio.controller.admin.job.vo.JobDeployReqVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.job.vo.JobPageReqVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.job.vo.JobStatisticsRespVO;
import cn.iocoder.yudao.module.datastudio.dal.mysql.job.DataJobMapper;
import cn.iocoder.yudao.module.datastudio.framework.flink.client.FlinkApiFactory;
import cn.iocoder.yudao.module.datastudio.service.file.SqlEditService;
import cn.iocoder.yudao.module.datastudio.service.flinkcluster.FlinkClusterService;
import cn.iocoder.yudao.module.datastudio.service.job.DataJobService;
import cn.iocoder.yudao.module.datastudio.service.job.FlinkJarService;
import cn.iocoder.yudao.module.flink.common.api.FlinkApi;
import cn.iocoder.yudao.module.flink.common.dal.dataobject.FlinkJobDeployDO;
import cn.iocoder.yudao.module.flink.common.dto.JobCancelReqDto;
import cn.iocoder.yudao.module.flink.common.enums.JobTypeEnum;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 作业管理 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Slf4j
public class DataJobServiceImpl implements DataJobService {

  @Resource private DataJobMapper dataJobMapper;
  @Autowired private SqlEditService sqlEditService;
  @Autowired private FlinkJarService flinkJarService;
  @Autowired private FlinkClusterService flinkClusterService;

  @Override
  public Long createJob(JobDeployReqVO saveReqVO) {

    return 1L;
  }

  @Override
  public void deleteJob(Long id) {
    FlinkJobDeployDO job = dataJobMapper.selectById(id);
    if (job == null) {
      throw ServiceExceptionUtil.exception(new ErrorCode(500, "作业不存在"));
    }
    dataJobMapper.deleteById(id);
  }

  @Override
  public void deleteJobList(List<Long> ids) {
    if (CollectionUtil.isEmpty(ids)) {
      return;
    }
    dataJobMapper.deleteByIds(ids);
  }

  @Override
  public List<FlinkJobDeployDO> getJobList(String jobName, String status, String executionMode) {
    LambdaQueryWrapperX<FlinkJobDeployDO> wrapper = new LambdaQueryWrapperX<>();
    wrapper
        .likeIfPresent(FlinkJobDeployDO::getJobName, jobName)
        .eqIfPresent(FlinkJobDeployDO::getStatus, status)
        .eqIfPresent(FlinkJobDeployDO::getExecutionMode, executionMode)
        .orderByDesc(FlinkJobDeployDO::getSubmitTime);

    return dataJobMapper.selectList(wrapper);
  }

  @Override
  public PageResult<FlinkJobDeployDO> getJobPage(JobPageReqVO pageReqVO) {
    return dataJobMapper.selectPage(
        pageReqVO,
        new LambdaQueryWrapperX<FlinkJobDeployDO>()
            .likeIfPresent(FlinkJobDeployDO::getJobName, pageReqVO.getJobName())
            .eqIfPresent(FlinkJobDeployDO::getStatus, pageReqVO.getStatus())
            .eqIfPresent(FlinkJobDeployDO::getExecutionMode, pageReqVO.getExecutionMode())
            .orderByDesc(FlinkJobDeployDO::getSubmitTime));
  }

  @Override
  public FlinkJobDeployDO getJob(Long id) {
    return dataJobMapper.selectById(id);
  }

  @Override
  public void stopJob(Long id) {
    FlinkJobDeployDO job = dataJobMapper.selectById(id);
    if (job == null) {
      throw ServiceExceptionUtil.exception(new ErrorCode(500, "作业不存在"));
    }
    FlinkApi flinkApi = FlinkApiFactory.getFlinkApiByVersion(job.getFlinkVersion());
    JobCancelReqDto reqDto = new JobCancelReqDto();
    reqDto.setConfig(job.getConfig());
    reqDto.setJobId(job.getJobId());
    flinkApi.cancelJob(reqDto).getCheckedData();
    job.setStatus(JobStatus.CANCELED);
    job.setEndTime(LocalDateTime.now());
    // 计算执行时长
    if (job.getStartTime() != null) {
      long duration = java.time.Duration.between(job.getStartTime(), job.getEndTime()).toMillis();
      job.setDuration(duration);
    }
    dataJobMapper.updateById(job);
    log.info("停止作业成功: {}", job.getJobName());
  }

  @Override
  public FlinkJobDeployDO getJobByFlinkJobId(String flinkJobId) {
    if (!StringUtils.hasText(flinkJobId)) {
      return null;
    }
    return dataJobMapper.selectByFlinkJobId(flinkJobId);
  }

  @Override
  public List<FlinkJobDeployDO> getRecentJobs(Integer limit) {
    return dataJobMapper.selectRecentList(limit != null ? limit : 20);
  }

  @Override
  public void startJob(Long id) {
    FlinkJobDeployDO job = dataJobMapper.selectById(id);
    if (job == null) {
      throw ServiceExceptionUtil.exception(new ErrorCode(500, "作业不存在"));
    }

    job.setStatus(JobStatus.RUNNING);
    job.setStartTime(LocalDateTime.now());
    dataJobMapper.updateById(job);

    log.info("启动作业成功: {}", job.getJobName());
  }

  @Override
  public JobStatisticsRespVO getJobStatistics() {
    JobStatisticsRespVO statistics = new JobStatisticsRespVO();

    // 统计各状态作业数量
    Map<JobStatus, Long> statusCountMap =
        dataJobMapper.selectList(null).stream()
            .collect(Collectors.groupingBy(FlinkJobDeployDO::getStatus, Collectors.counting()));

    statistics.setTotalCount(dataJobMapper.selectCount(null));
    statistics.setRunningCount(statusCountMap.getOrDefault(JobStatus.RUNNING, 0L));
    statistics.setSuccessCount(statusCountMap.getOrDefault(JobStatus.FINISHED, 0L));
    statistics.setFailedCount(statusCountMap.getOrDefault(JobStatus.FAILED, 0L));
    statistics.setCancelledCount(statusCountMap.getOrDefault(JobStatus.CANCELED, 0L));

    return statistics;
  }

  @Override
  public String deployJob(JobDeployReqVO reqVO) {
    JobTypeEnum jobType = reqVO.getJobType();

    switch (jobType) {
      case JAR:
        return flinkJarService.deploy(reqVO);
      case FLINK_SQL:
        return sqlEditService.deploy(reqVO.getFileId());
      case DATA_INGESTION:
    }
    return "";
  }
}
