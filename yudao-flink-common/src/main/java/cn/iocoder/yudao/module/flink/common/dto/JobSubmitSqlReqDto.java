package cn.iocoder.yudao.module.flink.common.dto;

import lombok.Data;

/**
 * @author yzh
 */
@Data
public class JobSubmitSqlReqDto {
    private String sql;
    private FlinkConfig flinkConfig;
}
