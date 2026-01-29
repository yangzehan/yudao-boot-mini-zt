package cn.iocoder.yudao.module.datastudio.controller.admin.job;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.datastudio.controller.admin.job.vo.JobDeployReqVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.job.vo.JobPageReqVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.job.vo.JobRespVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.job.vo.JobStatisticsRespVO;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.flink.job.deploy.FlinkJobDeployDO;
import cn.iocoder.yudao.module.datastudio.service.job.DataJobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import javax.annotation.Resource;
import javax.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 作业管理 Controller
 *
 * @author 芋道源码
 */
@Tag(name = "数据中台 - 作业管理")
@RestController
@RequestMapping("/data-studio/job")
@Validated
public class DataJobController {

  @Resource private DataJobService dataJobService;

  @DeleteMapping("delete")
  @Operation(summary = "删除作业")
  @Parameter(name = "id", description = "作业ID", required = true, example = "1024")
  @PreAuthorize("@ss.hasPermission('datastudio:job:delete')")
  public CommonResult<Boolean> deleteJob(@RequestParam("id") Long id) {
    dataJobService.deleteJob(id);
    return success(true);
  }

  @DeleteMapping("/delete-list")
  @Operation(summary = "批量删除作业")
  @Parameter(name = "ids", description = "作业ID列表", required = true)
  @PreAuthorize("@ss.hasPermission('datastudio:job:delete')")
  public CommonResult<Boolean> deleteJobList(@RequestParam("ids") List<Long> ids) {
    dataJobService.deleteJobList(ids);
    return success(true);
  }

  @GetMapping("/list")
  @Operation(summary = "获取作业列表")
  @PreAuthorize("@ss.hasPermission('datastudio:job:query')")
  public CommonResult<List<JobRespVO>> getJobList(
      @RequestParam(value = "jobName", required = false) String jobName,
      @RequestParam(value = "status", required = false) String status,
      @RequestParam(value = "executionMode", required = false) String executionMode) {
    List<FlinkJobDeployDO> list = dataJobService.getJobList(jobName, status, executionMode);
    return success(BeanUtils.toBean(list, JobRespVO.class));
  }

  @GetMapping("/page")
  @Operation(summary = "获取作业分页")
  @PreAuthorize("@ss.hasPermission('datastudio:job:query')")
  public CommonResult<PageResult<JobRespVO>> getJobPage(@Valid JobPageReqVO pageReqVO) {
    PageResult<FlinkJobDeployDO> pageResult = dataJobService.getJobPage(pageReqVO);
    return success(BeanUtils.toBean(pageResult, JobRespVO.class));
  }

  @GetMapping("/get")
  @Operation(summary = "获取作业")
  @Parameter(name = "id", description = "作业ID", required = true, example = "1024")
  @PreAuthorize("@ss.hasPermission('datastudio:job:query')")
  public CommonResult<JobRespVO> getJob(@RequestParam("id") Long id) {
    FlinkJobDeployDO job = dataJobService.getJob(id);
    return success(BeanUtils.toBean(job, JobRespVO.class));
  }

  @GetMapping("/get-by-job-id")
  @Operation(summary = "根据 Flink 作业ID获取作业")
  @Parameter(name = "flinkJobId", description = "Flink作业ID", required = true)
  @PreAuthorize("@ss.hasPermission('datastudio:job:query')")
  public CommonResult<JobRespVO> getJobByFlinkJobId(@RequestParam("flinkJobId") String flinkJobId) {
    FlinkJobDeployDO job = dataJobService.getJobByFlinkJobId(flinkJobId);
    return success(BeanUtils.toBean(job, JobRespVO.class));
  }

  @GetMapping("/recent")
  @Operation(summary = "获取最近的作业执行记录")
  @PreAuthorize("@ss.hasPermission('datastudio:job:query')")
  public CommonResult<List<JobRespVO>> getRecentJobs(
      @RequestParam(value = "limit", defaultValue = "20") Integer limit) {
    List<FlinkJobDeployDO> list = dataJobService.getRecentJobs(limit);
    return success(BeanUtils.toBean(list, JobRespVO.class));
  }

  @PostMapping("/start")
  @Operation(summary = "启动作业")
  @Parameter(name = "id", description = "作业ID", required = true, example = "1024")
  @PreAuthorize("@ss.hasPermission('datastudio:job:start')")
  public CommonResult<Boolean> startJob(@RequestParam("id") Long id) {
    dataJobService.startJob(id);
    return success(true);
  }

  @PostMapping("/stop")
  @Operation(summary = "停止作业")
  @Parameter(name = "id", description = "作业ID", required = true, example = "1024")
  @PreAuthorize("@ss.hasPermission('datastudio:job:stop')")
  public CommonResult<Boolean> stopJob(@RequestParam("id") Long id) {
    dataJobService.stopJob(id);
    return success(true);
  }

  @PostMapping("/deploy")
  @Operation(summary = "部署作业")
  @Parameter(name = "id", description = "作业ID", required = true, example = "1024")
  @PreAuthorize("@ss.hasPermission('datastudio:job:deploy')")
  public CommonResult<String> deployJob(@RequestBody JobDeployReqVO reqVO) {
    return success(dataJobService.deployJob(reqVO));
  }

  @GetMapping("/statistics")
  @Operation(summary = "获取作业状态统计")
  @PreAuthorize("@ss.hasPermission('datastudio:job:query')")
  public CommonResult<JobStatisticsRespVO> getJobStatistics() {
    JobStatisticsRespVO statistics = dataJobService.getJobStatistics();
    return success(statistics);
  }
}
