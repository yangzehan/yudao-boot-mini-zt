package cn.iocoder.yudao.module.flink.deploy;

import static cn.hutool.core.exceptions.ExceptionUtil.wrapRuntime;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.spring.SpringUtils;
import cn.iocoder.yudao.module.flink.common.deployer.DeployParam;
import cn.iocoder.yudao.module.flink.common.deployer.FlinkJobDeployer;
import cn.iocoder.yudao.module.flink.common.dto.JobDeployRespDto;
import cn.iocoder.yudao.module.flink.deploy.base.AbstractFlinkJobDyploy;
import cn.iocoder.yudao.module.flink.deploy.param.DeployLocalJarParam;
import cn.iocoder.yudao.module.flink.deploy.param.DeployLocalSqlParam;
import cn.iocoder.yudao.module.flink.deploy.service.AsyncTaskService;
import java.io.Serializable;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.runtime.minicluster.MiniCluster;
import org.apache.flink.runtime.minicluster.MiniClusterConfiguration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * Flink作业本地执行器
 *
 * <p>负责在本地MiniCluster中执行Flink作业
 *
 * @author yzh
 */
@Slf4j
public class FlinkJobLocalDeployerImpl extends AbstractFlinkJobDyploy implements FlinkJobDeployer, Serializable {

  @Override
  public DeployParam validSupportDeploySqlParam(DeployParam deployParam) {
    return validateParam(deployParam, DeployLocalSqlParam.class);
  }

  @Override
  public DeployParam validSupportDeployJarParam(DeployParam deployParam) {
    return validateParam(deployParam, DeployLocalJarParam.class);
  }

  @Override
  public JobDeployRespDto deploySql(DeployParam deployParam) {
    DeployLocalSqlParam localParam = validateParam(deployParam, DeployLocalSqlParam.class);
    StreamExecutionEnvironment environment =
        StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(localParam.getConfiguration());
    // 实现ExecuteSqlParam接口
    DeploySqlParam sqlParam =
        new DeploySqlParam() {
          @Override
          public String getSql() {
            return localParam.getSql();
          }

          @Override
          public String getDeployMode() {
            return "local";
          }

          @Override
          public String getJobName() {
            return localParam.getJobName();
          }
        };
    return executeSqlTemplate(sqlParam, environment);
  }

  @Override
  public JobDeployRespDto deployJar(DeployParam deployParam) {
    DeployLocalJarParam localParam = validateParam(deployParam, DeployLocalJarParam.class);
    Configuration configuration = localParam.getConfiguration();
    if (StrUtil.isBlank(configuration.get(RestOptions.ADDRESS))) {
      configuration.set(RestOptions.ADDRESS, "localhost");
    }
    MiniClusterConfiguration miniClusterConfig =
        new MiniClusterConfiguration.Builder()
            .setNumTaskManagers(1)
            .setNumSlotsPerTaskManager(4)
            .setConfiguration(configuration)
            .build();
    MiniCluster miniCluster = new MiniCluster(miniClusterConfig);
    try {
      log.info("启动 MiniCluster");
      miniCluster.start();

      // 实现ExecuteJarParam接口
      DeployJarParam jarParam =
          new DeployJarParam() {
            @Override
            public String getJarFile() {
              return localParam.getJarFile();
            }

            @Override
            public String getEntryPointClassName() {
              return localParam.getEntryPointClassName();
            }

            @Override
            public String[] getArgument() {
              return localParam.getArgument();
            }

            @Override
            public String getDeployMode() {
              return "local";
            }

            @Override
            public String getJobName() {
              return localParam.getJobName();
            }
          };
      JobDeployRespDto respDto = submitJarTemplate(jarParam, configuration);
      respDto.getConfig().put(RestOptions.ADDRESS.key(), "localhost");

      SpringUtils.getBean(AsyncTaskService.class).monitorClusters(miniCluster);
      return respDto;
    } catch (Exception e) {
      throw wrapRuntime(e);
    }
  }

  @Override
  public void cancelJob(String jobId, Map<String, String> config) {
    cancelJobLocalAndRemote(config, jobId);
  }
}
