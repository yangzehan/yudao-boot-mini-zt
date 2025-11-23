package cn.iocoder.yudao.module.datastudio.controller.admin.dataIngestion.vo.version;

import cn.iocoder.yudao.module.datastudio.dal.dataobject.dataIngestion.DataIngestionVersionDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 数据摄取版本创建请求 VO
 *
 * @author 芋道源码
 */
@Data
@Schema(name = "数据摄取版本创建请求 VO")
public class DataIngestionVersionCreateReqVO {

    @Schema(name = "文件ID", required = true, example = "1")
    @NotNull(message = "文件ID不能为空")
    private Long dataIngestionId;

    @Schema(name = "文件内容", required = true)
    @NotEmpty(message = "文件内容不能为空")
    private String content;

    @Schema(name = "配置信息")
    private DataIngestionVersionDO.ConfigInfo config;

    @Schema(name = "版本备注", example = "这是版本备注")
    private String remark;

    @Schema(name = "版本类型", required = true, example = "manual")
    @NotEmpty(message = "版本类型不能为空")
    private String versionType; // manual-手动保存, auto-自动保存

}
