package cn.iocoder.yudao.module.datastudio.controller.admin.file.vo.save;

import cn.iocoder.yudao.module.datastudio.dto.flink.FlinkConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 * SQL编辑器配置保存请求VO
 *
 * @author 芋道源码
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(name = "SQL编辑器配置保存请求VO")
public class SqlEditConfigSaveReqVO {

    @Schema(name = "配置ID", example = "1")
    private Long id;

    @Schema(name = "SQL编辑器文件ID", required = true, example = "1024")
    @NotNull(message = "SQL编辑器文件ID不能为空")
    private Long sqlEditId;

    @Schema(name = "Flink配置信息", description = "SQL任务的执行配置")
    private FlinkConfig config;

}
