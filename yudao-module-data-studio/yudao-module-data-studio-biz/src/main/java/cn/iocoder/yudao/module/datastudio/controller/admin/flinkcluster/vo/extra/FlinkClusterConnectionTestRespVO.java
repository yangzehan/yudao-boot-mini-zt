package cn.iocoder.yudao.module.datastudio.controller.admin.flinkcluster.vo.extra;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Flink 集群连接测试响应 VO
 *
 * @author 芋道源码
 */
@Data
@Schema(description = "Flink 集群连接测试结果")
public class FlinkClusterConnectionTestRespVO {

    @Schema(description = "是否连接成功", example = "true")
    private Boolean connected;

    @Schema(description = "响应时间（毫秒）", example = "120")
    private Long responseTime;

    @Schema(description = "Flink版本", example = "1.17.1")
    private String flinkVersion;

    @Schema(description = "JobManager地址", example = "10.1.1.100:8081")
    private String jobManagerUrl;

    @Schema(description = "最后消息", example = "2024-01-20 14:35:00 连接成功")
    private String lastMessage;

}
