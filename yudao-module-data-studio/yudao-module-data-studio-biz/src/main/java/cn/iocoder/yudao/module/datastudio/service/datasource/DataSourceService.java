package cn.iocoder.yudao.module.datastudio.service.datasource;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.datastudio.controller.admin.datasource.vo.*;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.datasource.DataSourceDO;

import javax.validation.Valid;
import java.util.List;

/**
 * 数据源配置 Service 接口
 *
 * @author 芋道源码
 */
public interface DataSourceService {

    /**
     * 创建数据源配置
     *
     * @param createReqVO 创建信息
     * @return 数据源ID
     */
    Long createDataSource(@Valid DataSourceSaveReqVO createReqVO);

    /**
     * 更新数据源配置
     *
     * @param updateReqVO 更新信息
     */
    void updateDataSource(@Valid DataSourceSaveReqVO updateReqVO);

    /**
     * 删除数据源配置
     *
     * @param id 数据源ID
     */
    void deleteDataSource(Long id);

    /**
     * 批量删除数据源配置
     *
     * @param ids 数据源ID列表
     */
    void deleteDataSourceList(List<Long> ids);

    /**
     * 获得数据源配置
     *
     * @param id 数据源ID
     * @return 数据源配置
     */
    DataSourceDO getDataSource(Long id);

    /**
     * 获得数据源配置列表
     *
     * @param name 数据源名称
     * @param type 数据源类型
     * @param status 状态
     * @return 数据源配置列表
     */
    List<DataSourceDO> getDataSourceList(String name, String type, Integer status);

    /**
     * 获得数据源配置分页
     *
     * @param pageReqVO 分页查询
     * @return 数据源配置分页
     */
    PageResult<DataSourceDO> getDataSourcePage(DataSourcePageReqVO pageReqVO);

    /**
     * 测试数据源连接
     *
     * @param id 数据源ID
     * @return 连接是否成功
     */
    Boolean testConnection(Long id);

    /**
     * 测试数据源连接（直接传入配置）
     *
     * @param saveReqVO 数据源配置
     * @return 连接是否成功
     */
    Boolean testConnection(DataSourceSaveReqVO saveReqVO);

    /**
     * 获取数据源类型列表
     *
     * @return 类型列表
     */
    List<DataSourceTypeRespVO> getDataSourceTypes();

    /**
     * 获取数据源的表列表
     *
     * @param id 数据源ID
     * @return 表名列表
     */
    List<String> getTables(Long id);

}
