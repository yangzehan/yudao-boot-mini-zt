package cn.iocoder.yudao.module.datastudio.service.file;



import cn.iocoder.yudao.module.datastudio.controller.admin.file.vo.file.SqlEditListReqVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.file.vo.save.SqlEditSaveReqVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.file.vo.resp.SqlEditRespVO;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.file.SqlEditDO;

import java.util.List;

/**
 * SQL 编辑器 Service 接口
 *
 * @author 芋道源码
 */
public interface SqlEditService {

    /**
     * 创建文件/文件夹
     *
     * @param createReqVO 创建请求VO
     * @return 文件ID
     */
    Long createFile(SqlEditSaveReqVO createReqVO);

    /**
     * 更新文件/文件夹
     *
     * @param updateReqVO 更新请求VO
     */
    void updateFile(SqlEditSaveReqVO updateReqVO);

    /**
     * 删除文件/文件夹
     *
     * @param id 文件ID
     */
    void deleteFile(Long id);

    /**
     * 批量删除文件/文件夹
     *
     * @param ids 文件ID列表
     */
    void deleteFileList(List<Long> ids);

    /**
     * 获取文件信息
     *
     * @param id 文件ID
     * @return 文件信息
     */
    SqlEditDO getFile(Long id);

    /**
     * 获取文件列表
     *
     * @param reqVO 筛选条件请求VO
     * @return 文件列表
     */
    List<SqlEditDO> getFileList(SqlEditListReqVO reqVO);

    /**
     * 获取树形文件结构
     *
     * @return 树形文件结构
     */
    List<SqlEditRespVO> getFileTree();

    /**
     * 获取指定父目录下的文件列表
     *
     * @param parentId 父ID
     * @return 文件列表
     */
    List<SqlEditRespVO> getFilesByParentId(Long parentId);

    /**
     * 搜索文件
     *
     * @param keyword 关键词
     * @return 文件列表
     */
    List<SqlEditRespVO> searchFiles(String keyword);

    /**
     * 移动文件
     *
     * @param id 文件ID
     * @param targetParentId 目标父ID
     */
    void moveFile(Long id, Long targetParentId);

    /**
     * 重命名文件
     *
     * @param id 文件ID
     * @param name 新名称
     */
    void renameFile(Long id, String name);

    /**
     * 保存文件内容
     *
     * @param id 文件ID
     * @param content 文件内容
     * @deprecated 使用 saveFileData 代替
     */
    @Deprecated
    void saveFileContent(Long id, String content);

    /**
     * 保存文件数据（内容和配置）
     *
     * @param saveReqVO 文件数据
     */
    void saveFileData(SqlEditSaveReqVO saveReqVO);

    /**
     * 获取文件内容
     *
     * @param id 文件ID
     * @return 文件内容
     */
    String getFileContent(Long id);

}
