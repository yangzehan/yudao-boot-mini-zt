package cn.iocoder.yudao.module.datastudio.controller.admin.flinkcluster.vo.extra;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * Flink 集群统计信息响应 VO
 *
 * @author 芋道源码
 */
@Data
@Schema(description = "Flink 集群统计信息")
public class FlinkClusterStatisticsRespVO {

    @Schema(description = "总集群数", example = "10")
    private Integer total;

    @Schema(description = "远程集群数", example = "6")
    private Integer remoteCount;

    @Schema(description = "Yarn集群数", example = "4")
    private Integer yarnCount;

    @Schema(description = "运行中集群数", example = "7")
    private Integer runningCount;

    @Schema(description = "已停止集群数", example = "1")
    private Integer stoppedCount;

    @Schema(description = "可用集群数", example = "2")
    private Integer availableCount;

    @Schema(description = "不可用集群数", example = "0")
    private Integer unavailableCount;

    @Schema(description = "按项目统计")
    private Map<String, Integer> byProject;

}
