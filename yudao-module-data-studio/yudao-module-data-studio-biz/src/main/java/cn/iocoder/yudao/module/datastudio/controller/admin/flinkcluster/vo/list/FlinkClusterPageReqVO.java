package cn.iocoder.yudao.module.datastudio.controller.admin.flinkcluster.vo.list;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Flink 集群配置分页请求 VO
 *
 * @author 芋道源码
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Flink 集群配置分页请求 VO")
public class FlinkClusterPageReqVO extends PageParam {

    @Schema(description = "集群类型：remote-远程集群，yarn-Flink on Yarn", example = "remote")
    private String type;

    @Schema(description = "集群状态：running-运行中，stopped-已停止，available-可用，unavailable-不可用", example = "running")
    private String status;

    @Schema(description = "关键词搜索（搜索name、description）", example = "flink")
    private String keyword;

}
