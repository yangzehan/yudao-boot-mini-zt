package cn.iocoder.yudao.module.datastudio.controller.admin.file.vo.version;

import cn.iocoder.yudao.module.datastudio.dto.flink.FlinkConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * SQL编辑器版本创建请求 VO
 *
 * @author 芋道源码
 */
@Data
@Schema(name = "SQL编辑器版本创建请求 VO")
public class SqlEditVersionCreateReqVO {

    @Schema(name = "文件ID", required = true, example = "1")
    @NotNull(message = "文件ID不能为空")
    private Long sqlEditId;

    @Schema(name = "脚本内容", required = true)
    @NotEmpty(message = "脚本内容不能为空")
    private String content;

    @Schema(name = "Flink配置")
    private FlinkConfig config;

    @Schema(name = "版本备注", example = "这是版本备注")
    private String remark;

    @Schema(name = "版本类型", required = true, example = "manual")
    @NotEmpty(message = "版本类型不能为空")
    private String versionType; // manual-手动保存, auto-自动保存

}
