package cn.iocoder.yudao.module.datastudio.controller.admin.job.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 作业状态统计 Response VO
 *
 * @author 芋道源码
 */
@Data
@Schema(name = "JobStatisticsRespVO", description = "作业状态统计 Response VO")
public class JobStatisticsRespVO {

    @Schema(name = "totalCount", description = "总数量")
    private Long totalCount;

    @Schema(name = "runningCount", description = "运行中数量")
    private Long runningCount;

    @Schema(name = "successCount", description = "成功数量")
    private Long successCount;

    @Schema(name = "failedCount", description = "失败数量")
    private Long failedCount;

    @Schema(name = "pendingCount", description = "等待中数量")
    private Long pendingCount;

    @Schema(name = "cancelledCount", description = "已取消数量")
    private Long cancelledCount;

}
