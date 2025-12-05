package cn.iocoder.yudao.module.flink.common.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author yzh
 */
@Setter
@Getter
public class JobDeployJarReqDto {
    private FlinkConfig flinkConfig;
    private String jarFile;
    private String entryPointClassName;
    private String[] args;
    private String jobName;
}