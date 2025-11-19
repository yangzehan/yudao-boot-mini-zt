package cn.iocoder.yudao.module.datastudio.service.file;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.module.datastudio.controller.admin.file.vo.version.*;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.file.FlinkConfig;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.file.SqlEditVersionDO;

import java.util.List;

/**
 * SQL编辑器版本 Service 接口
 *
 * @author 芋道源码
 */
public interface SqlEditVersionService {

    /**
     * 创建版本
     *
     * @param sqlEditId 文件ID
     * @param content 脚本内容
     * @param config Flink配置
     * @param remark 版本备注
     * @param versionType 版本类型（manual/auto）
     * @return 版本ID
     */
    Long createVersion(Long sqlEditId, String content, FlinkConfig config, String remark, String versionType);

    /**
     * 获取版本列表（分页）
     *
     * @param sqlEditId 文件ID
     * @param pageParam 分页参数
     * @return 版本列表
     */
    PageResult<SqlEditVersionDO> getVersionList(Long sqlEditId, PageParam pageParam);

    /**
     * 获取版本列表（不分页）
     *
     * @param sqlEditId 文件ID
     * @return 版本列表
     */
    List<SqlEditVersionDO> getVersionList(Long sqlEditId);

    /**
     * 获取版本详情
     *
     * @param versionId 版本ID
     * @return 版本详情
     */
    SqlEditVersionDO getVersionDetail(Long versionId);

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
     * @param sqlEditId 文件ID
     * @param keepCount 保留版本数
     * @return 删除数量
     */
    Integer deleteOldVersions(Long sqlEditId, Integer keepCount);

}
