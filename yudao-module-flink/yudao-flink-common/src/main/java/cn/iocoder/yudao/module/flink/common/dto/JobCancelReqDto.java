package cn.iocoder.yudao.module.flink.common.dto;

import lombok.Data;

import java.util.Map;

/**
 * @author yzh
 */

@Data
public class JobCancelReqDto {
    private String jobId;

    private Map<String, String> config;


}
