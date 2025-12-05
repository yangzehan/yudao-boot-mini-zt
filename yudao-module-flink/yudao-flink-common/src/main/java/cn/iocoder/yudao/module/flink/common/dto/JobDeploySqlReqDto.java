package cn.iocoder.yudao.module.flink.common.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author yzh
 */
@Data
@Builder
public class JobDeploySqlReqDto {
    private String sql;
    private FlinkConfig flinkConfig;
    private Long clusterId;
    private String clusterName;
    private Long fileId;
    private String jobName;

}
