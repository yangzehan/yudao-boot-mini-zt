package cn.iocoder.yudao.module.datastudio.dal.mysql.dataIngestion;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.dataIngestion.DataIngestionConfigDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据摄取配置 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface DataIngestionConfigMapper extends BaseMapperX<DataIngestionConfigDO> {

    /**
     * 根据数据摄取文件ID查询配置
     *
     * @param dataIngestionId 数据摄取文件ID
     * @return 配置信息
     */
    default DataIngestionConfigDO selectByDataIngestionId(Long dataIngestionId) {
        return selectOne(new LambdaQueryWrapperX<DataIngestionConfigDO>()
                .eq(DataIngestionConfigDO::getDataIngestionId, dataIngestionId));
    }

    /**
     * 根据数据摄取文件ID删除配置
     *
     * @param dataIngestionId 数据摄取文件ID
     * @return 删除的数量
     */
    default int deleteByDataIngestionId(Long dataIngestionId) {
        return delete(new LambdaQueryWrapperX<DataIngestionConfigDO>()
                .eq(DataIngestionConfigDO::getDataIngestionId, dataIngestionId));
    }

}
