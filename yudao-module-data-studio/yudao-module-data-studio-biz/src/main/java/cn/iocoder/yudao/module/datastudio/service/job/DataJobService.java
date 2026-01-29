package cn.iocoder.yudao.module.datastudio.service.job;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.datastudio.controller.admin.job.vo.JobDeployReqVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.job.vo.JobPageReqVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.job.vo.JobStatisticsRespVO;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.flink.job.deploy.FlinkJobDeployDO;
import java.util.List;

/**
 * 作业管理 Service 接口
 *
 * @author 芋道源码
 */
public interface DataJobService {

  /**
   * 创建作业
   *
   * @param saveReqVO 创建信息
   * @return 作业ID
   */
  Long createJob(JobDeployReqVO saveReqVO);

  /**
   * 删除作业
   *
   * @param id 作业ID
   */
  void deleteJob(Long id);

  /**
   * 批量删除作业
   *
   * @param ids 作业ID列表
   */
  void deleteJobList(List<Long> ids);

  /**
   * 获取作业列表
   *
   * @param jobName 作业名称（模糊搜索）
   * @param status 作业状态
   * @param executionMode 执行模式
   * @return 作业列表
   */
  List<FlinkJobDeployDO> getJobList(String jobName, String status, String executionMode);

  /**
   * 获取作业分页
   *
   * @param pageReqVO 分页查询
   * @return 作业分页
   */
  PageResult<FlinkJobDeployDO> getJobPage(JobPageReqVO pageReqVO);

  /**
   * 获取作业
   *
   * @param id 作业ID
   * @return 作业信息
   */
  FlinkJobDeployDO getJob(Long id);

  /**
   * 根据 Flink 作业ID获取作业信息
   *
   * @param flinkJobId Flink作业ID
   * @return 作业信息
   */
  FlinkJobDeployDO getJobByFlinkJobId(String flinkJobId);

  /**
   * 获取最近的作业执行记录
   *
   * @param limit 限制数量
   * @return 作业列表
   */
  List<FlinkJobDeployDO> getRecentJobs(Integer limit);

  /**
   * 启动作业
   *
   * @param id 作业ID
   */
  void startJob(Long id);

  /**
   * 停止作业
   *
   * @param id 作业ID
   */
  void stopJob(Long id);

  /**
   * 获取作业状态统计
   *
   * @return 状态统计
   */
  JobStatisticsRespVO getJobStatistics();

  String deployJob(JobDeployReqVO reqVO);
}
