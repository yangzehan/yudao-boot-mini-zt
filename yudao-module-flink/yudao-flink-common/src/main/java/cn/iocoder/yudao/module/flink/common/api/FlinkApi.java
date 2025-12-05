package cn.iocoder.yudao.module.flink.common.api;


import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.flink.common.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.List;

/**
 * @author yzh
 */
public interface FlinkApi {

    String getVersion();

    @PostMapping("/flink/job/deploy")
    ResponseEntity<JobDeployRespDto> deployJob(@Valid @RequestBody JobDeployRequest request);

    @GetMapping("/flink/job/{jobId}/status")
    ResponseEntity<JobStatusResponse> getJobStatus(@PathVariable(value = "jobId") String jobId);

    @PostMapping("/flink/job/cancel")
    CommonResult<Boolean> cancelJob(@RequestBody JobCancelReqDto reqDto);

    @GetMapping("/flink/job/running")
    ResponseEntity<List<JobStatusResponse>> getRunningJobs();

    @GetMapping("/flink/job/list")
    ResponseEntity<List<JobStatusResponse>> getJobList();

    @PostMapping("/flink/job/deploy-sql")
    CommonResult<JobDeployRespDto> deploySql(@RequestBody JobDeploySqlReqDto request);

    @PostMapping("/flink/job/deploy-jar")
    CommonResult<JobDeployRespDto> deployJar(@RequestBody JobDeployJarReqDto request);

}
