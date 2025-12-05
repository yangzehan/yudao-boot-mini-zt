package cn.iocoder.yudao.module.flink.common.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.flink.common.dal.dataobject.FlinkJobDeployDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Flink 任务执行记录 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface FlinkJobDeployInfoMapper extends BaseMapperX<FlinkJobDeployDO> {

    /**
     * 根据文件ID查询执行记录列表
     *
     * @param fileId 文件ID
     * @return 执行记录列表
     */
    default List<FlinkJobDeployDO> selectListByFileId(Long fileId) {
        return selectList(new LambdaQueryWrapperX<FlinkJobDeployDO>()
                .eq(FlinkJobDeployDO::getFileId, fileId));
    }

    /**
     * 根据集群ID查询执行记录列表
     *
     * @param clusterId 集群ID
     * @return 执行记录列表
     */
    default List<FlinkJobDeployDO> selectListByClusterId(Long clusterId) {
        return selectList(new LambdaQueryWrapperX<FlinkJobDeployDO>()
                .eq(FlinkJobDeployDO::getClusterId, clusterId));
    }

    /**
     * 根据状态查询执行记录列表
     *
     * @param status 状态
     * @return 执行记录列表
     */
    default List<FlinkJobDeployDO> selectListByStatus(String status) {
        return selectList(new LambdaQueryWrapperX<FlinkJobDeployDO>()
                .eq(FlinkJobDeployDO::getStatus, status));
    }

}