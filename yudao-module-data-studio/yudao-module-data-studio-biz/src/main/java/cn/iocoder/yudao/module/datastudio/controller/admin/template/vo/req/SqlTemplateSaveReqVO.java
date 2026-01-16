package cn.iocoder.yudao.module.datastudio.controller.admin.template.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * SQL 模板保存 Request VO（创建/更新）
 *
 * @author admin
 */
@Data
@Schema(description = "SQL 模板保存 Request VO")
public class SqlTemplateSaveReqVO {

    @Schema(description = "模板ID", example = "1")
    private Long id;

    @Schema(description = "模板名称", required = true, example = "测试模板")
    @NotBlank(message = "模板名称不能为空")
    @Size(max = 128, message = "模板名称长度不能超过128个字符")
    private String name;

    @Schema(description = "模板描述", example = "这是一个测试模板")
    @Size(max = 500, message = "模板描述长度不能超过500个字符")
    private String description;

    @Schema(description = "模板分类", required = true, example = "sql")
    @NotBlank(message = "模板分类不能为空")
    @Size(max = 64, message = "模板分类长度不能超过64个字符")
    private String category;

    @Schema(description = "SQL模板内容", required = true)
    @NotBlank(message = "模板内容不能为空")
    private String content;

    @Schema(description = "默认Flink配置（JSON格式）")
    private String defaultConfig;

    @Schema(description = "显示顺序", example = "0")
    private Integer sort;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "占位符配置（JSON格式，存储每个占位符的标签和提示信息）")
    private String placeholderConfig;

}
