package cn.iocoder.yudao.module.flink.deploy;

import static cn.hutool.core.exceptions.ExceptionUtil.wrapRuntime;
import static org.apache.flink.streaming.api.environment.ExecutionCheckpointingOptions.CHECKPOINTING_INTERVAL;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.module.flink.common.deployer.DeployParam;
import cn.iocoder.yudao.module.flink.common.deployer.FlinkJobDeployer;
import cn.iocoder.yudao.module.flink.common.dto.JobDeployRespDto;
import cn.iocoder.yudao.module.flink.deploy.base.AbstractFlinkJobDyploy;
import cn.iocoder.yudao.module.flink.deploy.deployer.DataIngestionDeployer;
import cn.iocoder.yudao.module.flink.deploy.deployer.DataIngestionDeployerImpl;
import cn.iocoder.yudao.module.flink.deploy.enums.DeployModeEnum;
import cn.iocoder.yudao.module.flink.deploy.param.DeployLocalDataIngestionParam;
import cn.iocoder.yudao.module.flink.deploy.param.DeployLocalJarParam;
import cn.iocoder.yudao.module.flink.deploy.param.DeployLocalSqlParam;
import cn.iocoder.yudao.module.flink.deploy.service.AsyncTaskService;
import cn.iocoder.yudao.module.flink.deploy.service.impl.AsyncTaskServiceImpl;
import java.lang.reflect.Constructor;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.JobExecutionResult;
import org.apache.flink.cdc.cli.parser.PipelineDefinitionParser;
import org.apache.flink.cdc.cli.parser.YamlPipelineDefinitionParser;
import org.apache.flink.cdc.common.pipeline.PipelineOptions;
import org.apache.flink.cdc.composer.PipelineExecution;
import org.apache.flink.cdc.composer.definition.PipelineDef;
import org.apache.flink.cdc.composer.flink.FlinkPipelineComposer;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.CoreOptions;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.core.execution.JobClient;
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
public class FlinkJobLocalDeployerImpl extends AbstractFlinkJobDyploy implements FlinkJobDeployer {

  @Override
  public JobDeployRespDto deploySql(DeployParam deployParam) {
    DeployLocalSqlParam localParam = validateParam(deployParam, DeployLocalSqlParam.class);
    StreamExecutionEnvironment environment;
    if (ObjUtil.isNull(localParam.getConfiguration())) {
      environment = StreamExecutionEnvironment.getExecutionEnvironment();
    } else {
      environment =
          StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(localParam.getConfiguration());
      // 启用检查点配置（使用配置中的间隔和路径）
      Configuration config = localParam.getConfiguration();
      // 配置类加载器策略为 child-first，解决 Nacos SPI 类加载问题
      config.setString("classloader.resolve-order", "child-first");
      if (config.contains(CHECKPOINTING_INTERVAL)) {
        long checkpointInterval = config.get(CHECKPOINTING_INTERVAL).toMillis();
        environment.enableCheckpointing(checkpointInterval);
      } else {
        environment.enableCheckpointing(5000L);
      }

      // 设置 parallelism（配置已通过 Configuration 传递）
      if (config.contains(CoreOptions.DEFAULT_PARALLELISM)) {
        int parallelism = config.get(CoreOptions.DEFAULT_PARALLELISM);
        environment.setParallelism(parallelism);
      }
    }
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
  public JobDeployRespDto deployJar(DeployParam deployParam, boolean async) {
    DeployLocalJarParam localParam = validateParam(deployParam, DeployLocalJarParam.class);
    Configuration configuration = localParam.getConfiguration();
    if (StrUtil.isBlank(configuration.get(RestOptions.ADDRESS))) {
      configuration.set(RestOptions.ADDRESS, "localhost");
    }
    log.info("有效配置:{}", configuration);
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

      // 直接实例化 AsyncTaskService（不依赖 Spring）
      AsyncTaskService asyncTaskService = new AsyncTaskServiceImpl();
      asyncTaskService.monitorClusters(miniCluster, async);
      return respDto;
    } catch (Exception e) {
      throw wrapRuntime(e);
    }
  }

  @Override
  public void cancelJob(String jobId, Map<String, String> config) {
    cancelJobLocalAndRemote(config, jobId);
  }

  @Override
  public JobDeployRespDto deployDataIngestion(DeployParam deployParam) {
    DeployLocalDataIngestionParam localDeployParam =
        validateParam(deployParam, DeployLocalDataIngestionParam.class);
    DataIngestionDeployer dataIngestionDeployer = new DataIngestionDeployerImpl();
    return dataIngestionDeployer.deployLocal(localDeployParam);
  }
}
