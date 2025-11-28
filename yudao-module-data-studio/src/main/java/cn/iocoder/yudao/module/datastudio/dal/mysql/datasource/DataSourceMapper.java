package cn.iocoder.yudao.module.datastudio.dal.mysql.datasource;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.datastudio.controller.admin.datasource.vo.DataSourcePageReqVO;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.datasource.DataSourceDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 数据源配置 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface DataSourceMapper extends BaseMapperX<DataSourceDO> {

    default PageResult<DataSourceDO> selectPage(DataSourcePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<DataSourceDO>()
                .likeIfPresent(DataSourceDO::getName, reqVO.getName())
                .eqIfPresent(DataSourceDO::getType, reqVO.getType())
                .eqIfPresent(DataSourceDO::getStatus, reqVO.getStatus())
                .orderByDesc(DataSourceDO::getId));
    }

    default List<DataSourceDO> selectList(String name, String type, Integer status) {
        return this.selectList(new LambdaQueryWrapperX<DataSourceDO>()
                .likeIfPresent(DataSourceDO::getName, name)
                .eqIfPresent(DataSourceDO::getType, type)
                .eqIfPresent(DataSourceDO::getStatus, status)
                .orderByDesc(DataSourceDO::getId));
    }

    default DataSourceDO selectByName(String name) {
        return selectOne(DataSourceDO::getName, name);
    }

}
