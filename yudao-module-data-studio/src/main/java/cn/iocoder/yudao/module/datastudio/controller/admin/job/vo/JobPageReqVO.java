package cn.iocoder.yudao.module.datastudio.controller.admin.job.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 作业管理分页查询 Request VO
 *
 * @author 芋道源码
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(name = "JobPageReqVO", description = "作业管理分页查询 Request VO")
public class JobPageReqVO extends PageParam {

    @Schema(name = "jobName", description = "作业名称")
    private String jobName;

    @Schema(name = "status", description = "作业状态")
    private String status;

    @Schema(name = "executionMode", description = "执行模式")
    private String executionMode;

}
