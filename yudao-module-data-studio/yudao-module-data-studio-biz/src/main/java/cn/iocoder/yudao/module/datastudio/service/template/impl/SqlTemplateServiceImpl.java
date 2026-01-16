package cn.iocoder.yudao.module.datastudio.service.template.impl;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.datastudio.controller.admin.file.vo.save.SqlEditSaveReqVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.template.vo.SqlTemplateSaveReqVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.template.vo.req.*;
import cn.iocoder.yudao.module.datastudio.controller.admin.template.vo.resp.SqlTemplateRespVO;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.file.SqlEditDO;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.template.SqlTemplateDO;
import cn.iocoder.yudao.module.datastudio.dal.mysql.file.SqlEditMapper;
import cn.iocoder.yudao.module.datastudio.dal.mysql.template.SqlTemplateMapper;
import cn.iocoder.yudao.module.datastudio.service.template.SqlTemplateService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * SQL 模板 Service 实现
 *
 * @author admin
 */
@Slf4j
@Service
public class SqlTemplateServiceImpl implements SqlTemplateService {

  private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

  @Resource private SqlTemplateMapper sqlTemplateMapper;

  @Resource private SqlEditMapper sqlEditMapper;

  @Override
  public List<SqlTemplateRespVO> getTemplateList(SqlTemplateListReqVO reqVO) {
    LambdaQueryWrapper<SqlTemplateDO> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(SqlTemplateDO::getStatus, 1);
    if (StrUtil.isNotBlank(reqVO.getCategory())) {
      wrapper.eq(SqlTemplateDO::getCategory, reqVO.getCategory());
    }
    wrapper.orderByAsc(SqlTemplateDO::getSort);
    List<SqlTemplateDO> list = sqlTemplateMapper.selectList(wrapper);
    return list.stream().map(this::convertToRespVO).collect(Collectors.toList());
  }

  @Override
  public PageResult<SqlTemplateRespVO> getTemplatePage(SqlTemplatePageReqVO reqVO) {
    Page<SqlTemplateDO> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
    LambdaQueryWrapper<SqlTemplateDO> wrapper = new LambdaQueryWrapper<>();
    if (StrUtil.isNotBlank(reqVO.getName())) {
      wrapper.like(SqlTemplateDO::getName, reqVO.getName());
    }
    if (StrUtil.isNotBlank(reqVO.getCategory())) {
      wrapper.eq(SqlTemplateDO::getCategory, reqVO.getCategory());
    }
    if (reqVO.getStatus() != null) {
      wrapper.eq(SqlTemplateDO::getStatus, reqVO.getStatus());
    }
    wrapper.orderByAsc(SqlTemplateDO::getSort);
    IPage<SqlTemplateDO> pageResult = sqlTemplateMapper.selectPage(page, wrapper);
    List<SqlTemplateRespVO> voList =
        pageResult.getRecords().stream().map(this::convertToRespVO).collect(Collectors.toList());
    return new PageResult<>(voList, pageResult.getTotal());
  }

  @Override
  public SqlTemplateRespVO getTemplate(Long id) {
    SqlTemplateDO template = sqlTemplateMapper.selectById(id);
    if (template == null) {
      throw ServiceExceptionUtil.exception(new ErrorCode(1, "模板不存在"));
    }
    return convertToRespVO(template);
  }

  @Override
  public SqlTemplateDO getTemplateEntity(Long id) {
    return sqlTemplateMapper.selectById(id);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Long createFileFromTemplate(
      Long templateId, String fileName, Long parentId, Map<String, String> placeholderValues) {
    // 获取模板
    SqlTemplateDO template = sqlTemplateMapper.selectById(templateId);
    if (template == null) {
      throw ServiceExceptionUtil.exception(new ErrorCode(1, "模板不存在"));
    }

    // 验证父文件夹是否存在
    if (parentId != null && parentId > 0) {
      SqlEditDO parent = sqlEditMapper.selectById(parentId);
      if (parent == null || !"folder".equals(parent.getType())) {
        throw ServiceExceptionUtil.exception(new ErrorCode(1, "父文件夹不存在"));
      }
    }

    // 处理模板内容（替换占位符）
    String content = template.getContent();
    if (placeholderValues != null && !placeholderValues.isEmpty()) {
      for (Map.Entry<String, String> entry : placeholderValues.entrySet()) {
        content =
            content.replace(
                "${" + entry.getKey() + "}", entry.getValue() != null ? entry.getValue() : "");
      }
    }

    // 生成文件路径
    String filePath = generateFilePath(parentId, fileName);

    // 创建文件
    SqlEditSaveReqVO createReqVO = new SqlEditSaveReqVO();
    createReqVO.setName(fileName.endsWith(".sql") ? fileName : fileName + ".sql");
    createReqVO.setType("sql");
    createReqVO.setParentId(parentId != null ? parentId : 0L);
    createReqVO.setFilePath(filePath);
    createReqVO.setContent(content);
    createReqVO.setSort(0);
    createReqVO.setStatus(1);

    Long tenantId = TenantContextHolder.getTenantId();
    SqlEditDO file = BeanUtils.toBean(createReqVO, SqlEditDO.class);
    file.setTenantId(tenantId);
    sqlEditMapper.insert(file);

    return file.getId();
  }

  @Override
  public List<String> getTemplateCategories() {
    LambdaQueryWrapper<SqlTemplateDO> wrapper = new LambdaQueryWrapper<>();
    wrapper.select(SqlTemplateDO::getCategory);
    wrapper.eq(SqlTemplateDO::getStatus, 1);
    List<SqlTemplateDO> list = sqlTemplateMapper.selectList(wrapper);
    return list.stream()
        .map(SqlTemplateDO::getCategory)
        .filter(Objects::nonNull)
        .distinct()
        .sorted()
        .collect(Collectors.toList());
  }

  @Override
  public Set<String> parsePlaceholders(String content) {
    Set<String> placeholders = new LinkedHashSet<>();
    Matcher matcher = PLACEHOLDER_PATTERN.matcher(content);
    while (matcher.find()) {
      placeholders.add(matcher.group(1));
    }
    return placeholders;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Long createTemplate(SqlTemplateSaveReqVO reqVO) {
    SqlTemplateDO template = BeanUtils.toBean(reqVO, SqlTemplateDO.class);
    Long tenantId = TenantContextHolder.getTenantId();
    template.setTenantId(tenantId);
    if (template.getSort() == null) {
      template.setSort(0);
    }
    if (template.getStatus() == null) {
      template.setStatus(1);
    }
    sqlTemplateMapper.insert(template);
    return template.getId();
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void updateTemplate(SqlTemplateSaveReqVO reqVO) {
    SqlTemplateDO template = sqlTemplateMapper.selectById(reqVO.getId());
    if (template == null) {
      throw ServiceExceptionUtil.exception(new ErrorCode(1, "模板不存在"));
    }
    SqlTemplateDO updateTemplate = BeanUtils.toBean(reqVO, SqlTemplateDO.class);
    sqlTemplateMapper.updateById(updateTemplate);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void deleteTemplate(Long id) {
    SqlTemplateDO template = sqlTemplateMapper.selectById(id);
    if (template == null) {
      throw ServiceExceptionUtil.exception(new ErrorCode(1, "模板不存在"));
    }
    sqlTemplateMapper.deleteById(id);
  }

  /** 转换为响应VO */
  private SqlTemplateRespVO convertToRespVO(SqlTemplateDO template) {
    SqlTemplateRespVO respVO = BeanUtils.toBean(template, SqlTemplateRespVO.class);
    // 设置完整内容
    respVO.setContent(template.getContent());
    // 截取预览内容（500字符）
    if (template.getContent() != null) {
      String preview = template.getContent();
      if (preview.length() > 500) {
        preview = preview.substring(0, 500) + "...";
      }
      respVO.setPreviewContent(preview);
    }
    return respVO;
  }

  /** 生成文件路径 */
  private String generateFilePath(Long parentId, String fileName) {
    if (parentId == null || parentId == 0) {
      return "/project/" + fileName;
    }
    SqlEditDO parent = sqlEditMapper.selectById(parentId);
    if (parent == null) {
      return "/project/" + fileName;
    }
    String parentPath = parent.getFilePath();
    if (parentPath == null || parentPath.isEmpty()) {
      parentPath = "/project";
    }
    return parentPath + "/" + fileName;
  }
}
