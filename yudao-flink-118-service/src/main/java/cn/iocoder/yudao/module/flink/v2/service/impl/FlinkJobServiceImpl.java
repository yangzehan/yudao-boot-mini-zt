package cn.iocoder.yudao.module.flink.v2.service.impl;

import cn.iocoder.yudao.module.flink.common.dto.JobStatusResponse;
import cn.iocoder.yudao.module.flink.common.dto.JobSubmitRequest;
import cn.iocoder.yudao.module.flink.common.dto.JobSubmitrespDto;
import cn.iocoder.yudao.module.flink.v2.service.FlinkJobService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FlinkJobServiceImpl implements FlinkJobService {
    @Override
    public JobSubmitrespDto submitJob(JobSubmitRequest request) {
        return null;
    }

    @Override
    public JobStatusResponse getJobStatus(String jobId) {
        return null;
    }

    @Override
    public void cancelJob(String jobId) {

    }

    @Override
    public List<JobStatusResponse> getRunningJobs() {
        return List.of();
    }

    @Override
    public List<JobStatusResponse> getJobList() {
        return List.of();
    }
}
