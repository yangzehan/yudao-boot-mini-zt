package cn.iocoder.yudao.module.datastudio.dal.mysql.file;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.file.SqlEditConfigDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * SQL编辑器配置 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface SqlEditConfigMapper extends BaseMapperX<SqlEditConfigDO> {

    /**
     * 根据SQL编辑器文件ID查询配置
     *
     * @param sqlEditId SQL编辑器文件ID
     * @return 配置信息
     */
    default SqlEditConfigDO selectBySqlEditId(Long sqlEditId) {
        return selectOne(new LambdaQueryWrapperX<SqlEditConfigDO>()
                .eq(SqlEditConfigDO::getSqlEditId, sqlEditId));
    }

    /**
     * 根据SQL编辑器文件ID删除配置
     *
     * @param sqlEditId SQL编辑器文件ID
     * @return 删除的数量
     */
    default int deleteBySqlEditId(Long sqlEditId) {
        return delete(new LambdaQueryWrapperX<SqlEditConfigDO>()
                .eq(SqlEditConfigDO::getSqlEditId, sqlEditId));
    }

}
