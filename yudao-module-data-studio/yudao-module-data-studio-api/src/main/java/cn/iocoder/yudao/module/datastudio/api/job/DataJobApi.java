package cn.iocoder.yudao.module.datastudio.api.job;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.datastudio.api.enums.JobStatus;
import cn.iocoder.yudao.module.datastudio.api.job.dto.DataJobDto;
import cn.iocoder.yudao.module.datastudio.api.job.dto.DataJobUpdateDto;
import java.util.List;
import javax.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient("yudao-server")
public interface DataJobApi {

  /**
   * 更新作业
   *
   * @param dto 作业更新参数
   * @return 是否更新成功
   */
  @PostMapping("/data-studio/job/update")
  CommonResult<Boolean> updateJob(@Valid @RequestBody DataJobUpdateDto dto);

  /**
   * 批量更新作业
   *
   * @param dtos 批量作业更新参数
   * @return 是否更新成功
   */
  @PostMapping("/data-studio/job/update-batch")
  CommonResult<Boolean> updateJobBatch(@Valid @RequestBody List<DataJobUpdateDto> dtos);

  /**
   * 根据webInterfaceURL更新作业，调用该api则说明原集群已经停止
   *
   * @param webInterfaceURL 作业更新参数
   * @return 是否更新成功
   */
  @GetMapping("/data-studio/job/update-by-web-interface-url")
  CommonResult<Boolean> updateJob(@RequestParam(value = "webInterfaceURL") String webInterfaceURL);

  @PostMapping("/data-studio/job/list")
  CommonResult<List<DataJobDto>> listJob(
      @RequestParam("jobStatusList") List<JobStatus> jobStatusList,
      @RequestParam("flinkVersion") String flinkVersion);

  @GetMapping("/data-studio/job/should-monitor/{jobId}")
  CommonResult<Boolean> shouldMonitor(@PathVariable(value = "jobId") String jobId);

  @GetMapping("/data-studio/job/monitor-job-finished")
  CommonResult<Boolean> monitorJobFinished(
      @RequestParam("JobId") String jobId, @RequestParam("jobStatus") JobStatus jobStatus);
}
