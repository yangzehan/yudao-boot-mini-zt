package cn.iocoder.yudao.module.datastudio.controller.admin.template.vo.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * SQL 模板详情 Response VO
 *
 * @author admin
 */
@Data
@Schema(description = "SQL 模板详情 Response VO")
public class SqlTemplateDetailRespVO {

    @Schema(description = "模板ID", example = "1")
    private Long id;

    @Schema(description = "模板名称", example = "测试模板")
    private String name;

    @Schema(description = "模板描述", example = "这是一个测试模板")
    private String description;

    @Schema(description = "模板分类", example = "sql")
    private String category;

    @Schema(description = "完整模板内容")
    private String content;

    @Schema(description = "默认Flink配置（JSON格式）")
    private String defaultConfig;

    @Schema(description = "占位符配置（JSON格式）")
    private String placeholderConfig;

    @Schema(description = "显示顺序", example = "0")
    private Integer sort;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "创建者")
    private String creator;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

}
