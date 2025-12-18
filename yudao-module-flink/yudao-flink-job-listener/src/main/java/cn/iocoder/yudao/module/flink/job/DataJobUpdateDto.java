package cn.iocoder.yudao.module.flink.job;

/**
 * @author yzh
 */
public class DataJobUpdateDto {
  private String jobId;
  private String status;

  public DataJobUpdateDto(String jobId, String status) {
    this.jobId = jobId;
    this.status = status;
  }

  public String getJobId() {
    return jobId;
  }

  public void setJobId(String jobId) {
    this.jobId = jobId;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
