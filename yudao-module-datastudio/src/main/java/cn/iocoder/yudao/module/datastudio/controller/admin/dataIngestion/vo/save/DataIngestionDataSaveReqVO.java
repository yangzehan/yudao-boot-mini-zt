package cn.iocoder.yudao.module.datastudio.controller.admin.dataIngestion.vo.save;

import cn.iocoder.yudao.module.datastudio.dto.flink.FlinkConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 数据摄取数据保存请求VO（包含文件信息和配置信息）
 *
 * @author 芋道源码
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(name = "数据摄取数据保存请求VO")
public class DataIngestionDataSaveReqVO {

    @Schema(name = "文件ID", example = "1")
    private Long id;

    @Schema(name = "文件/文件夹名称", required = true, example = "我的文件")
    @NotEmpty(message = "文件名称不能为空")
    @Size(max = 100, message = "文件名称长度不能超过100个字符")
    private String name;

    @Schema(name = "文件类型：folder-文件夹，yaml-yaml文件，file-普通文件", required = true, example = "folder")
    @NotEmpty(message = "文件类型不能为空")
    @Size(max = 20, message = "文件类型长度不能超过20个字符")
    private String type;

    @Schema(name = "父文件夹ID")
    private Long parentId;

    @Schema(name = "文件路径", example = "/datastudio/ingestion/config.yaml")
    @Size(max = 500, message = "文件路径长度不能超过500个字符")
    private String filePath;

    @Schema(name = "文件内容")
    private String content;

    @Schema(name = "显示顺序", example = "0")
    private Integer sort;

    @Schema(name = "状态：0-禁用，1-启用", example = "1")
    private Integer status;

    @Schema(name = "Flink配置信息", description = "SQL任务的执行配置")
    private FlinkConfig config;

}
