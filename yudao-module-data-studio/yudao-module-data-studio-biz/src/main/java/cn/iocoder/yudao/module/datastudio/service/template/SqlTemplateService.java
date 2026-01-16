package cn.iocoder.yudao.module.datastudio.service.template;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.datastudio.controller.admin.template.vo.SqlTemplateSaveReqVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.template.vo.req.SqlTemplateListReqVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.template.vo.req.SqlTemplatePageReqVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.template.vo.resp.SqlTemplateRespVO;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.template.SqlTemplateDO;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * SQL 模板 Service 接口
 *
 * @author admin
 */
public interface SqlTemplateService {

  /** 获取模板列表（简单列表） */
  List<SqlTemplateRespVO> getTemplateList(SqlTemplateListReqVO reqVO);

  /** 分页获取模板列表（管理用） */
  PageResult<SqlTemplateRespVO> getTemplatePage(SqlTemplatePageReqVO reqVO);

  /** 获取模板详情 */
  SqlTemplateRespVO getTemplate(Long id);

  /** 获取模板实体 */
  SqlTemplateDO getTemplateEntity(Long id);

  /**
   * 根据模板创建文件
   *
   * @param templateId 模板ID
   * @param fileName 文件名称
   * @param parentId 父目录ID
   * @param placeholderValues 占位符替换值
   * @return 创建的文件ID
   */
  Long createFileFromTemplate(
      Long templateId, String fileName, Long parentId, Map<String, String> placeholderValues);

  /** 获取模板分类列表 */
  List<String> getTemplateCategories();

  /**
   * 解析模板中的占位符
   *
   * @param content 模板内容
   * @return 占位符集合
   */
  Set<String> parsePlaceholders(String content);

  /** 创建模板 */
  Long createTemplate(SqlTemplateSaveReqVO reqVO);

  /** 更新模板 */
  void updateTemplate(SqlTemplateSaveReqVO reqVO);

  /** 删除模板 */
  void deleteTemplate(Long id);
}
