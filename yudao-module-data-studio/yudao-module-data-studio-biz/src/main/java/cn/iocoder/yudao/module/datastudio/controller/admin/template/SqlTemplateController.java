package cn.iocoder.yudao.module.datastudio.controller.admin.template;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.datastudio.controller.admin.template.vo.SqlTemplateCreateFileReqVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.template.vo.SqlTemplateSaveReqVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.template.vo.req.*;
import cn.iocoder.yudao.module.datastudio.controller.admin.template.vo.resp.SqlTemplateDetailRespVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.template.vo.resp.SqlTemplateRespVO;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.template.SqlTemplateDO;
import cn.iocoder.yudao.module.datastudio.service.template.SqlTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Set;
import javax.annotation.Resource;
import javax.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * SQL 模板 Controller
 *
 * @author admin
 */
@Tag(name = "数据中台 - SQL 模板")
@RestController
@RequestMapping("/sql-template")
@Validated
public class SqlTemplateController {

  @Resource private SqlTemplateService sqlTemplateService;

  @GetMapping("/list")
  @Operation(summary = "获取模板列表")
  public CommonResult<List<SqlTemplateRespVO>> getTemplateList(SqlTemplateListReqVO reqVO) {
    return success(sqlTemplateService.getTemplateList(reqVO));
  }

  @GetMapping("/page")
  @Operation(summary = "分页获取模板列表（管理用）")
  @PreAuthorize("@ss.hasPermission('datastudio:template:query')")
  public CommonResult<PageResult<SqlTemplateRespVO>> getTemplatePage(SqlTemplatePageReqVO reqVO) {
    return success(sqlTemplateService.getTemplatePage(reqVO));
  }

  @GetMapping("/get")
  @Operation(summary = "获取模板详情")
  public CommonResult<SqlTemplateDetailRespVO> getTemplate(@RequestParam("id") Long id) {
    SqlTemplateDO template = sqlTemplateService.getTemplateEntity(id);
    SqlTemplateDetailRespVO respVO = new SqlTemplateDetailRespVO();
    respVO.setId(template.getId());
    respVO.setName(template.getName());
    respVO.setDescription(template.getDescription());
    respVO.setCategory(template.getCategory());
    respVO.setContent(template.getContent());
    respVO.setDefaultConfig(template.getDefaultConfig());
    respVO.setPlaceholderConfig(template.getPlaceholderConfig());
    respVO.setSort(template.getSort());
    respVO.setStatus(template.getStatus());
    respVO.setCreateTime(template.getCreateTime());
    respVO.setCreator(template.getCreator());
    respVO.setUpdateTime(template.getUpdateTime());
    return success(respVO);
  }

  @GetMapping("/parse-placeholders")
  @Operation(summary = "解析模板中的占位符")
  public CommonResult<Set<String>> parsePlaceholders(@RequestParam("content") String content) {
    if (!StringUtils.hasText(content)) {
      return success(java.util.Collections.emptySet());
    }
    return success(sqlTemplateService.parsePlaceholders(content));
  }

  @PostMapping("/create-file")
  @Operation(summary = "根据模板创建文件")
  @PreAuthorize("@ss.hasPermission('datastudio:file:create')")
  public CommonResult<Long> createFileFromTemplate(
      @Valid @RequestBody SqlTemplateCreateFileReqVO reqVO) {
    return success(
        sqlTemplateService.createFileFromTemplate(
            reqVO.getTemplateId(),
            reqVO.getFileName(),
            reqVO.getParentId(),
            reqVO.getPlaceholderValues()));
  }

  @GetMapping("/categories")
  @Operation(summary = "获取模板分类列表")
  public CommonResult<List<String>> getTemplateCategories() {
    return success(sqlTemplateService.getTemplateCategories());
  }

  // ========== 模板管理 API ==========

  @PostMapping("/create")
  @Operation(summary = "创建模板")
  @PreAuthorize("@ss.hasPermission('datastudio:template:create')")
  public CommonResult<Long> createTemplate(@Valid @RequestBody SqlTemplateSaveReqVO reqVO) {
    return success(sqlTemplateService.createTemplate(reqVO));
  }

  @PutMapping("/update")
  @Operation(summary = "更新模板")
  @PreAuthorize("@ss.hasPermission('datastudio:template:update')")
  public CommonResult<Boolean> updateTemplate(@Valid @RequestBody SqlTemplateSaveReqVO reqVO) {
    sqlTemplateService.updateTemplate(reqVO);
    return success(true);
  }

  @DeleteMapping("/delete")
  @Operation(summary = "删除模板")
  @Parameter(name = "id", description = "模板ID", required = true)
  @PreAuthorize("@ss.hasPermission('datastudio:template:delete')")
  public CommonResult<Boolean> deleteTemplate(@RequestParam("id") Long id) {
    sqlTemplateService.deleteTemplate(id);
    return success(true);
  }
}
