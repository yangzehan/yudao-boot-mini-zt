package cn.iocoder.yudao.module.datastudio.dal.mysql.file;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.file.SqlEditVersionDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * SQL编辑器版本 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface SqlEditVersionMapper extends BaseMapperX<SqlEditVersionDO> {

    /**
     * 根据文件ID获取版本列表（分页）
     */
    default PageResult<SqlEditVersionDO> selectPageBySqlEditId(Long sqlEditId, Integer pageNo, Integer pageSize) {

        PageParam pageParam = new PageParam();
        pageParam.setPageNo(pageNo);
        pageParam.setPageSize(pageSize);
        return selectPage(pageParam,new LambdaQueryWrapperX<SqlEditVersionDO>()
                .eq(SqlEditVersionDO::getSqlEditId, sqlEditId)
                .orderByDesc(SqlEditVersionDO::getVersionNumber));
    }

    /**
     * 根据文件ID获取版本列表（不分页，按版本号倒序）
     */
    default List<SqlEditVersionDO> selectListBySqlEditId(Long sqlEditId) {
        return selectList(new LambdaQueryWrapperX<SqlEditVersionDO>()
                .eq(SqlEditVersionDO::getSqlEditId, sqlEditId)
                .orderByDesc(SqlEditVersionDO::getVersionNumber));
    }

    /**
     * 获取最新的版本号
     */
    default Long selectMaxVersionNumberBySqlEditId(Long sqlEditId) {
        SqlEditVersionDO version = selectOne(new LambdaQueryWrapperX<SqlEditVersionDO>()
                .eq(SqlEditVersionDO::getSqlEditId, sqlEditId)
                .orderByDesc(SqlEditVersionDO::getVersionNumber)
                .last("LIMIT 1"));
        return version != null ? version.getVersionNumber() : 0L;
    }

    /**
     * 获取文件的版本总数
     */
    default Long selectCountBySqlEditId(Long sqlEditId) {
        return selectCount(new LambdaQueryWrapperX<SqlEditVersionDO>()
                .eq(SqlEditVersionDO::getSqlEditId, sqlEditId));
    }

    /**
     * 删除文件的旧版本（保留最新N个版本）
     */
    default int deleteOldVersions(Long sqlEditId, Long keepCount) {
        return delete(new LambdaQueryWrapperX<SqlEditVersionDO>()
                .eq(SqlEditVersionDO::getSqlEditId, sqlEditId)
                .and(wrapper -> {
                    // 子查询：版本号 <= (最大版本号 - keepCount)
                    wrapper.le(SqlEditVersionDO::getVersionNumber,
                            "(SELECT version_number FROM data_studio_sql_edit_version WHERE sql_edit_id = " + sqlEditId +
                            " ORDER BY version_number DESC LIMIT 1 OFFSET " + keepCount + ")");
                }));
    }
}
