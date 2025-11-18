package cn.iocoder.yudao.module.datastudio.dal.mysql.dataIngestion;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.datastudio.controller.admin.dataIngestion.vo.file.DataIngestionListReqVO;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.dataIngestion.DataIngestionDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 数据摄取文件 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface DataIngestionMapper extends BaseMapperX<DataIngestionDO> {

    default List<DataIngestionDO> selectList(DataIngestionListReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<DataIngestionDO>()
                .likeIfPresent(DataIngestionDO::getName, reqVO.getKeyword())
                .eqIfPresent(DataIngestionDO::getType, reqVO.getType())
                .eqIfPresent(DataIngestionDO::getParentId, reqVO.getParentId())
                .orderByAsc(DataIngestionDO::getSort)
                .orderByDesc(DataIngestionDO::getId));
    }

    default List<DataIngestionDO> selectListByParentId(Long parentId) {
        return selectList(new LambdaQueryWrapperX<DataIngestionDO>()
                .eq(DataIngestionDO::getParentId, parentId)
                .orderByAsc(DataIngestionDO::getSort)
                .orderByDesc(DataIngestionDO::getId));
    }

    default List<DataIngestionDO> selectListByKeyword(String keyword) {
        return selectList(new LambdaQueryWrapperX<DataIngestionDO>()
                .like(DataIngestionDO::getName, keyword)
                .orderByAsc(DataIngestionDO::getSort)
                .orderByDesc(DataIngestionDO::getId));
    }

    default List<DataIngestionDO> selectListRoot() {
        return selectList(new LambdaQueryWrapperX<DataIngestionDO>()
                .or().isNull(DataIngestionDO::getParentId)
                .or().eq(DataIngestionDO::getParentId, 0L)
                .orderByAsc(DataIngestionDO::getSort)
                .orderByDesc(DataIngestionDO::getId));
    }

}
