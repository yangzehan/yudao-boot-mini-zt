package cn.iocoder.yudao.module.datastudio.controller.admin.template.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Map;

/**
 * 根据模板创建文件 Request VO
 *
 * @author admin
 */
@Data
@Schema(description = "根据模板创建文件 Request VO")
public class SqlTemplateCreateFileReqVO {

    @Schema(description = "模板ID", required = true, example = "1")
    @NotNull(message = "模板ID不能为空")
    private Long templateId;

    @Schema(description = "文件名称", required = true, example = "my_sql_file")
    @NotBlank(message = "文件名称不能为空")
    @Size(max = 100, message = "文件名称长度不能超过100个字符")
    private String fileName;

    @Schema(description = "父目录ID", example = "0")
    private Long parentId;

    @Schema(description = "占位符替换值")
    private Map<String, String> placeholderValues;

}
