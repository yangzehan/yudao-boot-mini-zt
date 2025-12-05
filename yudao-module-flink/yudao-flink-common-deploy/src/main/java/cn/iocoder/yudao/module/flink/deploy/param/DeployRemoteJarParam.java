package cn.iocoder.yudao.module.flink.deploy.param;

import cn.iocoder.yudao.module.flink.common.deployer.DeployParam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.flink.configuration.Configuration;

/**
 * @author yzh
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeployRemoteJarParam implements DeployParam {
    private Configuration configuration;
    private String jarFile;
    private String entryPointClassName;
    private String[] argument;
    private String jobName;
}