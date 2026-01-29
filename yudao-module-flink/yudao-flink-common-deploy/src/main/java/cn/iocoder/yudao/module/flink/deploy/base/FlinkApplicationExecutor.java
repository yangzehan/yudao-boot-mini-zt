package cn.iocoder.yudao.module.flink.deploy.base;

import cn.iocoder.yudao.module.flink.common.enums.JobTypeEnum;
import cn.iocoder.yudao.module.flink.deploy.FlinkJobLocalDeployerImpl;
import cn.iocoder.yudao.module.flink.deploy.constant.NacosConstant;
import cn.iocoder.yudao.module.flink.deploy.deployer.DataIngestionDeployer;
import cn.iocoder.yudao.module.flink.deploy.deployer.DataIngestionDeployerImpl;
import cn.iocoder.yudao.module.flink.deploy.param.DeployLocalDataIngestionParam;
import cn.iocoder.yudao.module.flink.deploy.param.DeployLocalSqlParam;
import cn.iocoder.yudao.module.flink.deploy.param.DeployRemoteJarParam;
import cn.iocoder.yudao.module.flink.job.RpcJobStatusHook;
import java.io.File;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.dag.Pipeline;
import org.apache.flink.client.program.PackagedProgram;
import org.apache.flink.client.program.PackagedProgramUtils;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.jobgraph.SavepointRestoreSettings;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.graph.StreamGraph;

/**
 * @author yzh
 */
@Slf4j
public class FlinkApplicationExecutor {

  public static void main(String[] args) {

    if (args.length != 4) {
      throw new RuntimeException("参数错误" + args.length);
    }
    String json = args[0];
    String type = args[1];
    String nacosAddr = args[2];
    String nacosNameSpace = args[3];
    NacosConstant.setDiscoveryServerAddr(nacosAddr);
    NacosConstant.setDiscoveryNamespace(nacosNameSpace);
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      if (type.equals(JobTypeEnum.FLINK_SQL.name())) {
        FlinkJobLocalDeployerImpl flinkJobLocalDeployer = new FlinkJobLocalDeployerImpl();
        log.info("执行SQL作业,参数为:{}", json);
        flinkJobLocalDeployer.deploySql(objectMapper.readValue(json, DeployLocalSqlParam.class));
      } else if (type.equals(JobTypeEnum.JAR.name())) {
        log.info("执行JAR作业,参数为:{}", json);
        StreamExecutionEnvironment executionEnvironment =
            StreamExecutionEnvironment.getExecutionEnvironment();
        DeployRemoteJarParam jarParam = objectMapper.readValue(json, DeployRemoteJarParam.class);

        PackagedProgram program =
            PackagedProgram.newBuilder()
                .setJarFile(new File(jarParam.getJarFile()))
                .setEntryPointClassName(jarParam.getEntryPointClassName())
                .setArguments(jarParam.getArgument())
                .setSavepointRestoreSettings(SavepointRestoreSettings.none())
                .build();
        Configuration configuration = (Configuration) executionEnvironment.getConfiguration();
        Pipeline pipeline =
            PackagedProgramUtils.getPipelineFromProgram(program, configuration, 1, false);
        StreamGraph streamGraph = (StreamGraph) pipeline;
        streamGraph.registerJobStatusHook(
            new RpcJobStatusHook(
                NacosConstant.getDiscoveryServerAddr(), NacosConstant.getDiscoveryNamespace()));
        executionEnvironment.execute(streamGraph);
      } else if (type.equals(JobTypeEnum.DATA_INGESTION.name())) {
        log.info("执行数据摄入作业,参数为:{}", json);
        DeployLocalDataIngestionParam dataIngestionParam =
            objectMapper.readValue(json, DeployLocalDataIngestionParam.class);
        DataIngestionDeployer dataIngestionDeployer = new DataIngestionDeployerImpl();
        dataIngestionDeployer.deployLocal(dataIngestionParam);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
