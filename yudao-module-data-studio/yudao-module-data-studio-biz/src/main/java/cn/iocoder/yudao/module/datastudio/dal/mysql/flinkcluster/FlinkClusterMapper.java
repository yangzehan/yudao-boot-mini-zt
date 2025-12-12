package cn.iocoder.yudao.module.datastudio.dal.mysql.flinkcluster;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.flinkcluster.FlinkClusterDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * Flink 集群配置 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface FlinkClusterMapper extends BaseMapperX<FlinkClusterDO> {

    /**
     * 根据集群类型查询列表
     *
     * @param type 集群类型
     * @return 集群列表
     */
    default List<FlinkClusterDO> selectListByType(String type) {
        return selectList(new LambdaQueryWrapperX<FlinkClusterDO>()
                .eq(FlinkClusterDO::getType, type));
    }

    /**
     * 根据状态查询列表
     *
     * @param status 集群状态
     * @return 集群列表
     */
    default List<FlinkClusterDO> selectListByStatus(String status) {
        return selectList(new LambdaQueryWrapperX<FlinkClusterDO>()
                .eq(FlinkClusterDO::getStatus, status));
    }

    /**
     * 关键词搜索（名称、描述）
     *
     * @param keyword 关键词
     * @return 集群列表
     */
    default List<FlinkClusterDO> selectListByKeyword(String keyword) {
        return selectList(new LambdaQueryWrapperX<FlinkClusterDO>()
                .like(FlinkClusterDO::getName, keyword)
                .or()
                .like(FlinkClusterDO::getDescription, keyword));
    }

    /**
     * 批量删除集群
     *
     * @param ids ID集合
     * @return 影响行数
     */
    default int deleteBatch(Collection<Long> ids) {
        return deleteBatchIds(ids);
    }

}
