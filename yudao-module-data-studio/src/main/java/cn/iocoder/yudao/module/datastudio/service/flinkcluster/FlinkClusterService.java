package cn.iocoder.yudao.module.datastudio.service.flinkcluster;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.datastudio.controller.admin.flinkcluster.vo.extra.FlinkClusterConnectionTestRespVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.flinkcluster.vo.extra.FlinkClusterStatisticsRespVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.flinkcluster.vo.extra.FlinkClusterTypeRespVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.flinkcluster.vo.extra.FlinkClusterVersionRespVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.flinkcluster.vo.list.FlinkClusterListReqVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.flinkcluster.vo.list.FlinkClusterPageReqVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.flinkcluster.vo.resp.FlinkClusterRespVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.flinkcluster.vo.save.FlinkClusterBatchDeleteReqVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.flinkcluster.vo.save.FlinkClusterSaveReqVO;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.flinkcluster.FlinkClusterDO;

import java.util.List;

/**
 * Flink 集群配置 Service 接口
 *
 * @author 芋道源码
 */
public interface FlinkClusterService {

    /**
     * 创建 Flink 集群配置
     *
     * @param createReqVO 创建信息
     * @return 集群ID
     */
    Long createFlinkCluster(FlinkClusterSaveReqVO createReqVO);

    /**
     * 更新 Flink 集群配置
     *
     * @param updateReqVO 更新信息
     */
    void updateFlinkCluster(FlinkClusterSaveReqVO updateReqVO);

    /**
     * 删除 Flink 集群配置
     *
     * @param id 集群ID
     */
    void deleteFlinkCluster(Long id);

    /**
     * 批量删除 Flink 集群配置
     *
     * @param deleteReqVO 批量删除信息
     */
    void deleteFlinkClusterList(FlinkClusterBatchDeleteReqVO deleteReqVO);

    /**
     * 获得 Flink 集群配置
     *
     * @param id 集群ID
     * @return Flink 集群配置
     */
    FlinkClusterDO getFlinkCluster(Long id);

    /**
     * 获得 Flink 集群配置列表
     *
     * @param listReqVO 查询条件
     * @return Flink 集群配置列表
     */
    List<FlinkClusterDO> getFlinkClusterList(FlinkClusterListReqVO listReqVO);

    /**
     * 获得 Flink 集群配置分页
     *
     * @param pageReqVO 分页查询
     * @return Flink 集群配置分页
     */
    PageResult<FlinkClusterDO> getFlinkClusterPage(FlinkClusterPageReqVO pageReqVO);

    /**
     * 测试集群连接
     *
     * @param id 集群ID
     * @return 连接测试结果
     */
    FlinkClusterConnectionTestRespVO testConnection(Long id);

    /**
     * 测试集群配置（不保存到数据库）
     *
     * @param clusterReqVO 集群配置
     * @return 连接测试结果
     */
    FlinkClusterConnectionTestRespVO testConnection(FlinkClusterSaveReqVO clusterReqVO);

    /**
     * 刷新集群状态
     *
     * @param id 集群ID
     * @return 更新后的集群信息
     */
    FlinkClusterDO refreshStatus(Long id);

    /**
     * 获取集群类型列表
     *
     * @return 集群类型列表
     */
    List<FlinkClusterTypeRespVO> getClusterTypes();

    /**
     * 获取 Flink 版本列表
     *
     * @return Flink 版本列表
     */
    List<FlinkClusterVersionRespVO> getFlinkVersions();

    /**
     * 获取集群统计信息
     *
     * @return 统计信息
     */
    FlinkClusterStatisticsRespVO getStatistics();

}
