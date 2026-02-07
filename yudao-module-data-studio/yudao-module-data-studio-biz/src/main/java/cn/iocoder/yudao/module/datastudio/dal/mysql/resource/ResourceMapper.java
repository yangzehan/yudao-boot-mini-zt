package cn.iocoder.yudao.module.datastudio.dal.mysql.resource;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.datastudio.controller.admin.resource.vo.ResourcePageReqVO;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.resource.ResourceDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 数据中台 - 资源 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ResourceMapper extends BaseMapperX<ResourceDO> {

    default PageResult<ResourceDO> selectPage(ResourcePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ResourceDO>()
                .likeIfPresent(ResourceDO::getName, reqVO.getName())
                .likeIfPresent(ResourceDO::getDescription, reqVO.getDescription())
                .orderByDesc(ResourceDO::getId));
    }

    default List<ResourceDO> selectList(String name) {
        return this.selectList(new LambdaQueryWrapperX<ResourceDO>()
                .likeIfPresent(ResourceDO::getName, name)
                .orderByDesc(ResourceDO::getId));
    }

}
