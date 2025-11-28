package cn.iocoder.yudao.module.flink.v2.service;

import cn.iocoder.yudao.module.flink.common.dto.JobStatusResponse;
import cn.iocoder.yudao.module.flink.common.dto.JobSubmitRequest;
import cn.iocoder.yudao.module.flink.common.dto.JobSubmitrespDto;

import java.util.List;

/**
 * Flink V2作业服务接口
 *
 * @author yzh
 * @since 2025-11-27
 */
public interface FlinkJobService {

    /**
     * 提交作业
     */
    public JobSubmitrespDto submitJob(JobSubmitRequest request);

    /**
     * 获取作业状态
     */
    public JobStatusResponse getJobStatus(String jobId);


    /**
     * 取消作业
     */
    public void cancelJob(String jobId);

    /**
     * 获取运行中作业列表
     */
    public List<JobStatusResponse> getRunningJobs();

    /**
     * 获取所有作业列表
     */
    public List<JobStatusResponse> getJobList();
}
