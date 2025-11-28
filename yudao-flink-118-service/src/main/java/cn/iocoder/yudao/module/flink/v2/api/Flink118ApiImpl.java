package cn.iocoder.yudao.module.flink.v2.api;


import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.flink.common.api.Flink118Api;
import cn.iocoder.yudao.module.flink.common.dto.*;
import cn.iocoder.yudao.module.flink.common.executor.FlinkJobExecutor;
import cn.iocoder.yudao.module.flink.common.factory.FlinkJobExecuteFactory;
import cn.iocoder.yudao.module.flink.v2.service.FlinkJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.configuration.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Flink V2作业管理控制器
 *
 * @author yzh
 * @since 2025-11-27
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
public class Flink118ApiImpl implements Flink118Api {

    private final FlinkJobService flinkJobService;

    /**
     * 提交作业
     */
    @Override
    public ResponseEntity<JobSubmitrespDto> submitJob(JobSubmitRequest request) {
        log.info("接收到Flink V2作业提交请求: {}", request);
        JobSubmitrespDto response = flinkJobService.submitJob(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取作业状态
     */
    @Override

    public ResponseEntity<JobStatusResponse> getJobStatus(String jobId) {
        log.info("获取作业状态: {}", jobId);
        JobStatusResponse response = flinkJobService.getJobStatus(jobId);
        return ResponseEntity.ok(response);
    }

    /**
     * 取消作业
     */
    @Override

    public ResponseEntity<Void> cancelJob(String jobId) {
        log.info("取消作业: {}", jobId);
        flinkJobService.cancelJob(jobId);
        return ResponseEntity.ok().build();
    }

    /**
     * 获取所有运行中作业
     */
    @Override

    public ResponseEntity<List<JobStatusResponse>> getRunningJobs() {
        List<JobStatusResponse> jobs = flinkJobService.getRunningJobs();
        return ResponseEntity.ok(jobs);
    }

    /**
     * 获取作业列表
     */
    @Override
    public ResponseEntity<List<JobStatusResponse>> getJobList() {
        List<JobStatusResponse> jobs = flinkJobService.getJobList();
        return ResponseEntity.ok(jobs);
    }

    @Override
    public CommonResult<JobSubmitrespDto> submitSql(JobSubmitSqlReqDto request) {
        log.info("flink版本{}开始提交sql作业", getVersion());

        log.info("解析flink配置文件");
        Configuration flinkConfig = parseFlinkConfig(request.getFlinkConfig());


        FlinkJobExecutor flinkJobExecutor = FlinkJobExecuteFactory.getExecutorFactoryByExecutionMode(request.getFlinkConfig().getExecutionMode());


        JobSubmitrespDto data = new JobSubmitrespDto();

        return CommonResult.success(data);

    }

    private Configuration parseFlinkConfig(FlinkConfig flinkConfig) {
        Configuration configuration = Configuration.fromMap(flinkConfig.getExtendedConfig());


        return null;
    }
}
