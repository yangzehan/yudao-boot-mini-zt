package cn.iocoder.yudao.module.flink.common.api;


import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.flink.common.dto.JobStatusResponse;
import cn.iocoder.yudao.module.flink.common.dto.JobSubmitRequest;
import cn.iocoder.yudao.module.flink.common.dto.JobSubmitrespDto;
import cn.iocoder.yudao.module.flink.common.dto.JobSubmitSqlReqDto;
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

    @PostMapping("/flink/submit")
    ResponseEntity<JobSubmitrespDto> submitJob(@Valid @RequestBody JobSubmitRequest request);

    @GetMapping("/flink/{jobId}/status")
    ResponseEntity<JobStatusResponse> getJobStatus(@PathVariable(value = "jobId") String jobId);

    @PostMapping("/flink/{jobId}/cancel")
    ResponseEntity<Void> cancelJob(@PathVariable(value = "jobId") String jobId);

    @GetMapping("/flink/running")
    ResponseEntity<List<JobStatusResponse>> getRunningJobs();

    @GetMapping("/flink/list")
    ResponseEntity<List<JobStatusResponse>> getJobList();

    @PostMapping("/flink/submitSql")
    CommonResult<JobSubmitrespDto> submitSql(JobSubmitSqlReqDto request);
}
