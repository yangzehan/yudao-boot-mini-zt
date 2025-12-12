package cn.iocoder.yudao.module.datastudio.controller.admin.flinkcluster.vo.save;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * Flink 集群配置保存请求 VO
 *
 * @author 芋道源码
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Flink 集群配置")
public class FlinkClusterSaveReqVO extends FlinkClusterBaseVO {

    @Schema(description = "集群ID（新增时不填）")
    private Long id;

}

