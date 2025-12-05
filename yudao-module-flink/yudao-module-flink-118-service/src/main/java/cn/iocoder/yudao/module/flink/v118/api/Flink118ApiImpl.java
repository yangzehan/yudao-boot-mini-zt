package cn.iocoder.yudao.module.flink.v118.api;


import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.flink.common.api.Flink118Api;
import cn.iocoder.yudao.module.flink.common.dto.*;
import cn.iocoder.yudao.module.flink.deploy.service.impl.FlinkJobDeployServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final FlinkJobDeployServiceImpl flinkJobDeployService;


    /**
     * 提交作业
     */
    @Override
    public ResponseEntity<JobDeployRespDto> deployJob(JobDeployRequest request) {
        log.info("接收到Flink V2作业提交请求: {}", request);
        JobDeployRespDto response = flinkJobDeployService.deployJob(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取作业状态
     */
    @Override

    public ResponseEntity<JobStatusResponse> getJobStatus(String jobId) {
        log.info("获取作业状态: {}", jobId);
        JobStatusResponse response = flinkJobDeployService.getJobStatus(jobId);
        return ResponseEntity.ok(response);
    }

    /**
     * 取消作业
     */
    @Override
    public CommonResult<Boolean> cancelJob(JobCancelReqDto reqDto) {
        log.info("取消作业: {}", reqDto);
        flinkJobDeployService.cancelJob(reqDto);
        return CommonResult.success(true);
    }

    /**
     * 获取所有运行中作业
     */
    @Override

    public ResponseEntity<List<JobStatusResponse>> getRunningJobs() {
        List<JobStatusResponse> jobs = null;
        return ResponseEntity.ok(jobs);
    }

    /**
     * 获取作业列表
     */
    @Override
    public ResponseEntity<List<JobStatusResponse>> getJobList() {
        List<JobStatusResponse> jobs = null;
        return ResponseEntity.ok(jobs);
    }

    /**
     * 提交Flink SQL作业
     *
     * @param request SQL作业提交请求参数，包含SQL语句和Flink配置信息
     * @return CommonResult<JobDeployRespDto> 作业提交结果，包含作业ID、作业名称、提交状态等信息
     */
    @Override
    public CommonResult<JobDeployRespDto> deploySql(JobDeploySqlReqDto request) {
        log.info("flink版本{}开始提交sql作业", getVersion());

        JobDeployRespDto respDto = flinkJobDeployService.deploySql(request);

        return CommonResult.success(respDto);

    }


    /**
     * 提交Flink Jar作业
     *
     * @param request Jar作业提交请求参数
     * @return CommonResult<JobDeployRespDto> 作业提交结果，包含作业ID、作业名称、提交状态等信息
     */
    @Override
    public CommonResult<JobDeployRespDto> deployJar(JobDeployJarReqDto request) {
        log.info("flink版本{}开始提交jar作业", getVersion());
        JobDeployRespDto respDto = flinkJobDeployService.deployJar(request);

        return CommonResult.success(respDto);

    }
}