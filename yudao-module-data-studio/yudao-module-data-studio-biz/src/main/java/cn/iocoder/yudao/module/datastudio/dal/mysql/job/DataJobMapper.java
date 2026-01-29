package cn.iocoder.yudao.module.datastudio.dal.mysql.job;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.flink.job.deploy.FlinkJobDeployDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * 作业管理 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface DataJobMapper extends BaseMapperX<FlinkJobDeployDO> {

  /**
   * 根据条件查询作业列表
   *
   * @param jobName 作业名称
   * @param status 作业状态
   * @param executionMode 执行模式
   * @return 作业列表
   */
  default List<FlinkJobDeployDO> selectList(String jobName, String status, String executionMode) {
    return selectList(
        new LambdaQueryWrapperX<FlinkJobDeployDO>()
            .likeIfPresent(FlinkJobDeployDO::getJobName, jobName)
            .eqIfPresent(FlinkJobDeployDO::getStatus, status)
            .eqIfPresent(FlinkJobDeployDO::getExecutionMode, executionMode)
            .orderByDesc(FlinkJobDeployDO::getSubmitTime));
  }

  /**
   * 根据 Flink 作业ID查询
   *
   * @param flinkJobId Flink作业ID
   * @return 作业信息
   */
  default FlinkJobDeployDO selectByFlinkJobId(String flinkJobId) {
    return selectOne(FlinkJobDeployDO::getJobId, flinkJobId);
  }

  /**
   * 查询最近的作业执行记录
   *
   * @param limit 限制数量
   * @return 作业列表
   */
  default List<FlinkJobDeployDO> selectRecentList(Integer limit) {
    return selectList(
        new LambdaQueryWrapperX<FlinkJobDeployDO>()
            .orderByDesc(FlinkJobDeployDO::getSubmitTime)
            .apply("limit " + limit));
  }

  /**
   * 根据状态查询作业数量统计
   *
   * @param status 作业状态
   * @return 作业数量
   */
  default Long selectCountByStatus(String status) {
    return selectCount(FlinkJobDeployDO::getStatus, status);
  }
}
