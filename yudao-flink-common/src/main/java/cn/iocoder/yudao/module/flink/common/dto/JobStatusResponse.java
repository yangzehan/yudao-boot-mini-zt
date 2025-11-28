package cn.iocoder.yudao.module.flink.common.dto;

import java.time.LocalDateTime;

public class JobStatusResponse {
    private String jobId;
    private String jobName;
    private String status;
    private String statusDesc;
    private LocalDateTime startTime;
    private LocalDateTime updateTime;
    private long duration;
    private int parallelism;
    private Object lastError;

    public void setJobId(String jobId) {

        this.jobId = jobId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobName(String jobName) {


        this.jobName = jobName;
    }

    public String getJobName() {
        return jobName;
    }

    public void setStatus(String status) {

        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatusDesc(String statusDesc) {

        this.statusDesc = statusDesc;
    }

    public String getStatusDesc() {
        return statusDesc;
    }

    public void setStartTime(LocalDateTime startTime) {

        this.startTime = startTime;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getDuration() {
        return duration;
    }

    public void setParallelism(int parallelism) {
        this.parallelism = parallelism;
    }

    public int getParallelism() {
        return parallelism;
    }

    public void setLastError(Object lastError) {
        this.lastError = lastError;
    }

    public Object getLastError() {
        return lastError;
    }
}
