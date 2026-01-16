package cn.iocoder.yudao.module.datastudio.controller.admin.template.vo.req;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * SQL 模板分页查询 Request VO
 *
 * @author admin
 */
@Data
@Schema(description = "SQL 模板分页查询 Request VO")
public class SqlTemplatePageReqVO extends PageParam {

  @Schema(description = "模板名称", example = "测试模板")
  private String name;

  @Schema(description = "模板分类", example = "sql")
  private String category;

  @Schema(description = "状态", example = "1")
  private Integer status;
}
