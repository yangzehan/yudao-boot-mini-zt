package cn.iocoder.yudao.module.datastudio.dal.mysql.dataIngestion;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.dataIngestion.DataIngestionVersionDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 数据摄取版本 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface DataIngestionVersionMapper extends BaseMapperX<DataIngestionVersionDO> {

    /**
     * 根据文件ID获取版本列表（分页）
     */
    default PageResult<DataIngestionVersionDO> selectPageByDataIngestionId(Long dataIngestionId, Integer pageNo, Integer pageSize) {
        PageParam pageParam = new PageParam();
        pageParam.setPageNo(pageNo);
        pageParam.setPageSize(pageSize);
        return selectPage(pageParam, new LambdaQueryWrapperX<DataIngestionVersionDO>()
                .eq(DataIngestionVersionDO::getDataIngestionId, dataIngestionId)
                .orderByDesc(DataIngestionVersionDO::getVersionNumber));
    }

    /**
     * 根据文件ID获取版本列表（不分页，按版本号倒序）
     */
    default List<DataIngestionVersionDO> selectListByDataIngestionId(Long dataIngestionId) {
        return selectList(new LambdaQueryWrapperX<DataIngestionVersionDO>()
                .eq(DataIngestionVersionDO::getDataIngestionId, dataIngestionId)
                .orderByDesc(DataIngestionVersionDO::getVersionNumber));
    }

    /**
     * 获取最新的版本号
     */
    default Long selectMaxVersionNumberByDataIngestionId(Long dataIngestionId) {
        DataIngestionVersionDO version = selectOne(new LambdaQueryWrapperX<DataIngestionVersionDO>()
                .eq(DataIngestionVersionDO::getDataIngestionId, dataIngestionId)
                .orderByDesc(DataIngestionVersionDO::getVersionNumber)
                .last("LIMIT 1"));
        return version != null ? version.getVersionNumber() : 0L;
    }

    /**
     * 获取文件的版本总数
     */
    default Long selectCountByDataIngestionId(Long dataIngestionId) {
        return selectCount(new LambdaQueryWrapperX<DataIngestionVersionDO>()
                .eq(DataIngestionVersionDO::getDataIngestionId, dataIngestionId));
    }

}
