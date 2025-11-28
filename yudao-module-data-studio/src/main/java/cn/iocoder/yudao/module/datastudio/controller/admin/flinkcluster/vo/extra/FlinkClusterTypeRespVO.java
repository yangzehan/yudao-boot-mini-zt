package cn.iocoder.yudao.module.datastudio.controller.admin.flinkcluster.vo.extra;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Flink 集群类型响应 VO
 *
 * @author 芋道源码
 */
@Data
@Schema(description = "集群类型")
public class FlinkClusterTypeRespVO {

    @Schema(description = "类型值", example = "remote")
    private String value;

    @Schema(description = "类型名称", example = "远程集群")
    private String label;

    @Schema(description = "是否可编辑", example = "true")
    private Boolean editable;

}
