package cn.iocoder.yudao.module.datastudio.service.dataIngestionVersion;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.dataIngestion.DataIngestionDO;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.dataIngestion.DataIngestionVersionDO;
import cn.iocoder.yudao.module.datastudio.dal.mysql.dataIngestion.DataIngestionMapper;
import cn.iocoder.yudao.module.datastudio.dal.mysql.dataIngestion.DataIngestionVersionMapper;
import cn.iocoder.yudao.module.datastudio.dto.flink.FlinkConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 数据摄取版本 Service 实现
 *
 * @author 芋道源码
 */
@Service
public class DataIngestionVersionServiceImpl implements DataIngestionVersionService {

    @Resource
    private DataIngestionVersionMapper versionMapper;

    @Resource
    private DataIngestionMapper dataIngestionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createVersion(Long dataIngestionId, String content, FlinkConfig config, String remark, String versionType) {
        // 1. 验证文件是否存在
        DataIngestionDO dataIngestion = dataIngestionMapper.selectById(dataIngestionId);
        if (dataIngestion == null) {
            throw new ServiceException(new ErrorCode(9999, "文件不存在"));
        }

        // 2. 获取当前最新版本号
        Long currentVersionNumber = versionMapper.selectMaxVersionNumberByDataIngestionId(dataIngestionId);
        Long nextVersionNumber = currentVersionNumber + 1;

        // 3. 创建版本记录
        DataIngestionVersionDO version = new DataIngestionVersionDO();
        version.setDataIngestionId(dataIngestionId);
        version.setVersionNumber(nextVersionNumber);
        version.setContent(content);
        version.setConfig(config);
        version.setRemark(remark);
        version.setVersionType(versionType);

        // 保存版本
        versionMapper.insert(version);

        // 4. 检查版本数量，如果超过7个则删除最旧的版本
        deleteOldVersions(dataIngestionId, 7);

        return version.getId();
    }

    @Override
    public PageResult<DataIngestionVersionDO> getVersionList(Long dataIngestionId, PageParam pageParam) {
        return versionMapper.selectPageByDataIngestionId(dataIngestionId, pageParam.getPageNo(), pageParam.getPageSize());
    }

    @Override
    public List<DataIngestionVersionDO> getVersionList(Long dataIngestionId) {
        return versionMapper.selectListByDataIngestionId(dataIngestionId);
    }

    @Override
    public DataIngestionVersionDO getVersionDetail(Long versionId) {
        DataIngestionVersionDO version = versionMapper.selectById(versionId);
        if (version == null) {
            throw new ServiceException(new ErrorCode(9999, "版本不存在"));
        }
        return version;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean rollbackToVersion(Long versionId) {
        // 1. 获取版本信息
        DataIngestionVersionDO version = versionMapper.selectById(versionId);
        if (version == null) {
            throw new ServiceException(new ErrorCode(9999, "版本不存在"));
        }

        // 2. 获取当前文件信息
        DataIngestionDO dataIngestion = dataIngestionMapper.selectById(version.getDataIngestionId());
        if (dataIngestion == null) {
            throw new ServiceException(new ErrorCode(9999, "文件不存在"));
        }

        // 3. 更新文件内容
        dataIngestion.setContent(version.getContent());
        dataIngestionMapper.updateById(dataIngestion);

        // 4. 创建回退操作版本记录
        String rollbackRemark = "回退到版本 v" + version.getVersionNumber();
        createVersion(dataIngestion.getId(), version.getContent(), version.getConfig(),
                rollbackRemark, "manual");

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteVersion(Long versionId) {
        // 1. 验证版本是否存在
        DataIngestionVersionDO version = versionMapper.selectById(versionId);
        if (version == null) {
            throw new ServiceException(new ErrorCode(9999, "版本不存在"));
        }

        // 2. 删除版本（逻辑删除）
        return versionMapper.deleteById(versionId) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer deleteOldVersions(Long dataIngestionId, Integer keepCount) {
        // 1. 获取当前版本总数
        Long totalCount = versionMapper.selectCountByDataIngestionId(dataIngestionId);
        if (totalCount <= keepCount) {
            return 0; // 版本数量未超过保留数量，无需删除
        }

        // 2. 计算需要删除的版本数量
        Long deleteCount = totalCount - keepCount;

        // 3. 获取需要删除的版本列表（最旧的那些）
        List<DataIngestionVersionDO> versionsToDelete = versionMapper.selectList(
                new LambdaQueryWrapperX<DataIngestionVersionDO>()
                        .eq(DataIngestionVersionDO::getDataIngestionId, dataIngestionId)
                        .orderByAsc(DataIngestionVersionDO::getVersionNumber)
                        .last("LIMIT " + deleteCount));

        // 4. 批量删除旧版本
        versionsToDelete.forEach(v -> versionMapper.deleteById(v.getId()));

        return versionsToDelete.size();
    }

}
