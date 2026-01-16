package cn.iocoder.yudao.module.flink.deploy.param;

import cn.iocoder.yudao.module.flink.common.deployer.DeployParam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.flink.configuration.Configuration;

/**
 * 远程数据摄入部署参数
 *
 * @author yzh
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeployRemoteDataIngestionParam implements DeployParam {
    private Configuration configuration;
    private String content;
    private String jobName;
    private Long clusterId;
    private String clusterName;
    private Long fileId;
}
