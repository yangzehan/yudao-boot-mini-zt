package cn.iocoder.yudao.module.datastudio.controller.admin.flinkcluster.vo.save;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Flink 集群批量删除请求 VO
 *
 * @author 芋道源码
 */
@Data
@Schema(description = "Flink 集群批量删除")
public class FlinkClusterBatchDeleteReqVO {

    @Schema(description = "集群ID列表", required = true, example = "[1, 2, 3]")
    @NotEmpty(message = "集群ID列表不能为空")
    private List<Long> ids;

}
