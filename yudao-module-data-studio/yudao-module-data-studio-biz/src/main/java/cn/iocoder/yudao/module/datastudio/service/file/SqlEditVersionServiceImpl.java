package cn.iocoder.yudao.module.datastudio.service.file;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.file.SqlEditConfigDO;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.file.SqlEditDO;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.file.SqlEditVersionDO;
import cn.iocoder.yudao.module.datastudio.dal.mysql.file.SqlEditConfigMapper;
import cn.iocoder.yudao.module.datastudio.dal.mysql.file.SqlEditMapper;
import cn.iocoder.yudao.module.datastudio.dal.mysql.file.SqlEditVersionMapper;
import cn.iocoder.yudao.module.flink.common.dto.FlinkConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * SQL编辑器版本 Service 实现
 *
 * @author 芋道源码
 */
@Service
public class SqlEditVersionServiceImpl implements SqlEditVersionService {

    @Resource
    private SqlEditVersionMapper versionMapper;

    @Resource
    private SqlEditConfigMapper sqlEditConfigMapper;

    @Resource
    private SqlEditMapper sqlEditMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long createVersion(Long sqlEditId, String content, FlinkConfig config, String remark, String versionType) {
        // 1. 验证文件是否存在
        SqlEditDO sqlEdit = sqlEditMapper.selectById(sqlEditId);
        if (sqlEdit == null) {
            throw new ServiceException(new ErrorCode(9999,"文件不存在"));
        }

        // 2. 获取当前最新版本号
        Long currentVersionNumber = versionMapper.selectMaxVersionNumberBySqlEditId(sqlEditId);
        Long nextVersionNumber = currentVersionNumber + 1;

        // 3. 创建版本记录
        SqlEditVersionDO version = new SqlEditVersionDO();
        version.setSqlEditId(sqlEditId);
        version.setVersionNumber(nextVersionNumber);
        version.setContent(content);
        version.setConfig(config);
        version.setRemark(remark);
        version.setVersionType(versionType);

        // 保存版本
        versionMapper.insert(version);

        // 4. 检查版本数量，如果超过7个则删除最旧的版本
        deleteOldVersions(sqlEditId, 7);

        return version.getId();
    }

    @Override
    public PageResult<SqlEditVersionDO> getVersionList(Long sqlEditId, PageParam pageParam) {
        return versionMapper.selectPageBySqlEditId(sqlEditId, pageParam.getPageNo(), pageParam.getPageSize());
    }

    @Override
    public List<SqlEditVersionDO> getVersionList(Long sqlEditId) {
        return versionMapper.selectListBySqlEditId(sqlEditId);
    }

    @Override
    public SqlEditVersionDO getVersionDetail(Long versionId) {
        SqlEditVersionDO version = versionMapper.selectById(versionId);
        if (version == null) {
            throw new ServiceException(new ErrorCode(9999,"版本不存在"));
        }
        return version;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean rollbackToVersion(Long versionId) {
        // 1. 获取版本信息
        SqlEditVersionDO version = versionMapper.selectById(versionId);
        if (version == null) {
            throw new ServiceException(new ErrorCode(9999,"版本不存在"));
        }

        // 2. 获取当前文件信息
        SqlEditDO sqlEdit = sqlEditMapper.selectById(version.getSqlEditId());
        if (sqlEdit == null) {
            throw new ServiceException(new ErrorCode(9999,"文件不存在"));
        }

        // 3. 更新文件内容
        sqlEdit.setContent(version.getContent());
        sqlEditMapper.updateById(sqlEdit);

        // 4. 如果版本有配置信息，也更新配置
        if (version.getConfig() != null) {
            SqlEditConfigDO configDO = new SqlEditConfigDO();
            configDO.setSqlEditId(version.getSqlEditId());
            configDO.setConfig(version.getConfig());
            // 先删除旧配置
            sqlEditConfigMapper.deleteBySqlEditId(version.getSqlEditId());
            // 保存新配置
            sqlEditConfigMapper.insert(configDO);
        }

        // 5. 创建回退操作版本记录
        createVersion(sqlEdit.getId(), version.getContent(), version.getConfig(),
                "回退到版本 v" + version.getVersionNumber(), "manual");

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteVersion(Long versionId) {
        // 1. 验证版本是否存在
        SqlEditVersionDO version = versionMapper.selectById(versionId);
        if (version == null) {
            throw new ServiceException(new ErrorCode(9999,"版本不存在"));
        }

        // 2. 删除版本（逻辑删除）
        return versionMapper.deleteById(versionId) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer deleteOldVersions(Long sqlEditId, Integer keepCount) {
        // 1. 获取当前版本总数
        Long totalCount = versionMapper.selectCountBySqlEditId(sqlEditId);
        if (totalCount <= keepCount) {
            return 0; // 版本数量未超过保留数量，无需删除
        }

        // 2. 计算需要删除的版本数量
        Long deleteCount = totalCount - keepCount;

        // 3. 获取需要删除的版本列表（最旧的那些）
        List<SqlEditVersionDO> versionsToDelete = versionMapper.selectList(
                new LambdaQueryWrapperX<SqlEditVersionDO>()
                        .eq(SqlEditVersionDO::getSqlEditId, sqlEditId)
                        .orderByAsc(SqlEditVersionDO::getVersionNumber)
                        .last("LIMIT " + deleteCount)
        );

        // 4. 批量删除
        for (SqlEditVersionDO version : versionsToDelete) {
            versionMapper.deleteById(version.getId());
        }

        return versionsToDelete.size();
    }



}
