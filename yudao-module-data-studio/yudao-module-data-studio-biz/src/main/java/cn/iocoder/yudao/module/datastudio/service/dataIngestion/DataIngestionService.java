package cn.iocoder.yudao.module.datastudio.service.dataIngestion;

import cn.iocoder.yudao.module.datastudio.controller.admin.dataIngestion.vo.file.DataIngestionListReqVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.dataIngestion.vo.resp.DataIngestionDataRespVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.dataIngestion.vo.resp.DataIngestionRespVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.dataIngestion.vo.save.DataIngestionDataSaveReqVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.dataIngestion.vo.save.DataIngestionSaveReqVO;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.dataIngestion.DataIngestionDO;
import java.util.List;

/**
 * 数据摄取 Service 接口
 *
 * @author 芋道源码
 */
public interface DataIngestionService {

  /**
   * 创建文件/文件夹
   *
   * @param createReqVO 创建信息
   * @return 文件ID
   */
  Long createFile(DataIngestionSaveReqVO createReqVO);

  /**
   * 更新文件/文件夹
   *
   * @param updateReqVO 更新信息
   */
  void updateFile(DataIngestionSaveReqVO updateReqVO);

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
   * 获取文件列表
   *
   * @param reqVO 查询条件
   * @return 文件列表
   */
  List<DataIngestionDO> getFileList(DataIngestionListReqVO reqVO);

  /**
   * 获取文件树形结构
   *
   * @return 文件树
   */
  List<DataIngestionRespVO> getFileTree();

  /**
   * 获取指定目录下的子文件列表
   *
   * @param parentId 父文件ID
   * @return 子文件列表
   */
  List<DataIngestionRespVO> getFilesByParentId(Long parentId);

  /**
   * 搜索文件
   *
   * @param keyword 搜索关键词
   * @return 文件列表
   */
  List<DataIngestionRespVO> searchFiles(String keyword);

  /**
   * 获取文件信息
   *
   * @param id 文件ID
   * @return 文件
   */
  DataIngestionDO getFile(Long id);

  /**
   * 移动文件
   *
   * @param id 文件ID
   * @param targetParentId 目标父文件夹ID
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

  /**
   * 保存文件数据和配置（同时创建版本）
   *
   * @param saveReqVO 保存信息
   */
  void saveFileData(DataIngestionDataSaveReqVO saveReqVO);

  /**
   * 获取文件数据（内容和配置）
   *
   * @param id 文件ID
   * @return 文件数据（包含内容和配置）
   */
  DataIngestionDataRespVO getFileData(Long id);

  String deploy(Long id);
}
