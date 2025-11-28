package cn.iocoder.yudao.module.datastudio.controller.admin.flinkcluster.vo.extra;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Flink 版本响应 VO
 *
 * @author 芋道源码
 */
@Data
@Schema(description = "Flink 版本")
public class FlinkClusterVersionRespVO {

    @Schema(description = "版本号", example = "1.18")
    private String version;

}
