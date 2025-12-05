package cn.iocoder.yudao.module.flink.common.service;

import cn.iocoder.yudao.module.flink.common.dto.*;

/**
 * Flink 任务执行记录 Service 接口
 *
 * @author 芋道源码
 */
public interface FlinkJobDeployService {

    /**
     * 提交作业
     */
    JobDeployRespDto deployJob(JobDeployRequest request);

    /**
     * 获取作业状态
     */
    JobStatusResponse getJobStatus(String jobId);


    /**
     * 取消作业
     */
    void cancelJob(JobCancelReqDto jobId);


    JobDeployRespDto deploySql(JobDeploySqlReqDto request);

    JobDeployRespDto deployJar(JobDeployJarReqDto request);
}