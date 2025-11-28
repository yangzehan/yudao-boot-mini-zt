package cn.iocoder.yudao.module.datastudio.service.dataIngestionVersion;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.dataIngestion.DataIngestionVersionDO;
import cn.iocoder.yudao.module.flink.common.dto.FlinkConfig;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 数据摄取版本 Service 接口
 *
 * @author 芋道源码
 */
public interface DataIngestionVersionService {

    /**
     * 创建版本
     *
     * @param dataIngestionId 文件ID
     * @param content 文件内容
     * @param config 配置信息
     * @param remark 版本备注
     * @param versionType 版本类型（manual/auto）
     * @return 版本ID
     */
    @Transactional(rollbackFor = Exception.class)
    Long createVersion(Long dataIngestionId, String content, FlinkConfig config, String remark, String versionType);

    /**
     * 获取版本列表（分页）
     *
     * @param dataIngestionId 文件ID
     * @param pageParam 分页参数
     * @return 版本列表
     */
    PageResult<DataIngestionVersionDO> getVersionList(Long dataIngestionId, PageParam pageParam);

    /**
     * 获取版本列表（不分页）
     *
     * @param dataIngestionId 文件ID
     * @return 版本列表
     */
    List<DataIngestionVersionDO> getVersionList(Long dataIngestionId);

    /**
     * 获取版本详情
     *
     * @param versionId 版本ID
     * @return 版本详情
     */
    DataIngestionVersionDO getVersionDetail(Long versionId);

    /**
     * 回退到指定版本
     *
     * @param versionId 版本ID
     * @return 是否成功
     */
    Boolean rollbackToVersion(Long versionId);

    /**
     * 删除版本
     *
     * @param versionId 版本ID
     * @return 是否成功
     */
    Boolean deleteVersion(Long versionId);

    /**
     * 清理旧版本（保留最新N个）
     *
     * @param dataIngestionId 文件ID
     * @param keepCount 保留版本数
     * @return 删除数量
     */
    Integer deleteOldVersions(Long dataIngestionId, Integer keepCount);

}
