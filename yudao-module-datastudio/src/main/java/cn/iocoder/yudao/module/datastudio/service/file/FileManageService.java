package cn.iocoder.yudao.module.datastudio.service.file;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.datastudio.controller.admin.file.vo.file.FileManageListReqVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.file.vo.save.FileManageSaveReqVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.file.vo.resp.FileManageRespVO;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.file.FileManageDO;

import java.util.List;

/**
 * 文件管理 Service 接口
 *
 * @author 芋道源码
 */
public interface FileManageService {

    /**
     * 创建文件/文件夹
     *
     * @param createReqVO 创建请求VO
     * @return 文件ID
     */
    Long createFile(FileManageSaveReqVO createReqVO);

    /**
     * 更新文件/文件夹
     *
     * @param updateReqVO 更新请求VO
     */
    void updateFile(FileManageSaveReqVO updateReqVO);

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
    FileManageDO getFile(Long id);

    /**
     * 获取文件列表
     *
     * @param reqVO 筛选条件请求VO
     * @return 文件列表
     */
    List<FileManageDO> getFileList(FileManageListReqVO reqVO);

    /**
     * 获取树形文件结构
     *
     * @return 树形文件结构
     */
    List<FileManageRespVO> getFileTree();

    /**
     * 获取指定父目录下的文件列表
     *
     * @param parentId 父ID
     * @return 文件列表
     */
    List<FileManageRespVO> getFilesByParentId(Long parentId);

    /**
     * 搜索文件
     *
     * @param keyword 关键词
     * @return 文件列表
     */
    List<FileManageRespVO> searchFiles(String keyword);

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
     */
    void saveFileContent(Long id, String content);

    /**
     * 获取文件内容
     *
     * @param id 文件ID
     * @return 文件内容
     */
    String getFileContent(Long id);

}
