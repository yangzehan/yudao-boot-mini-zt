package cn.iocoder.yudao.module.flink.deploy.base;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.module.flink.common.deployer.DeployParam;
import cn.iocoder.yudao.module.flink.common.dto.JobDeployRespDto;
import cn.iocoder.yudao.module.flink.common.util.SqlUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.JobID;
import org.apache.flink.client.deployment.ClusterRetrieveException;
import org.apache.flink.client.deployment.StandaloneClusterDescriptor;
import org.apache.flink.client.deployment.StandaloneClusterId;
import org.apache.flink.client.program.ClusterClient;
import org.apache.flink.client.program.ClusterClientProvider;
import org.apache.flink.client.program.PackagedProgram;
import org.apache.flink.client.program.PackagedProgramUtils;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.DeploymentOptions;
import org.apache.flink.configuration.PipelineOptions;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.core.execution.JobClient;
import org.apache.flink.runtime.jobgraph.JobGraph;
import org.apache.flink.runtime.jobgraph.SavepointRestoreSettings;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.bridge.java.StreamStatementSet;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.api.internal.TableEnvironmentImpl;
import org.apache.flink.table.operations.ModifyOperation;
import org.apache.flink.table.operations.Operation;
import org.apache.flink.table.operations.ShowOperation;
import org.apache.flink.table.planner.operations.PlannerQueryOperation;

import java.io.File;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Flink作业执行器抽象基类
 * <p>
 * 提供本地和远程执行器的公共模板方法，消除重复代码
 *
 * @author yzh
 */
@Slf4j
public abstract class AbstractFlinkJobDyploy {
    protected static void cancelJobLocalAndRemote(Map<String, String> config, String jobId) {
        try (StandaloneClusterDescriptor clusterDescriptor = new StandaloneClusterDescriptor(Configuration.fromMap(config))) {
            log.info("连接到远程集群");
            ClusterClientProvider<StandaloneClusterId> clusterClientProvider =
                    clusterDescriptor.retrieve(StandaloneClusterId.getInstance());
            log.info("获取ClusterClient实例");
            ClusterClient<StandaloneClusterId> clusterClient = clusterClientProvider.getClusterClient();
            if ("remote".equals(Configuration.fromMap(config).get(DeploymentOptions.TARGET))) {
                clusterClient.cancel(JobID.fromHexString(jobId));
            } else {
                clusterClient.shutDownCluster();
            }
        } catch (
                ClusterRetrieveException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 模板方法：提交JAR作业到远程集群
     * <p>
     * 封装了完整的JAR提交流程：
     * 1. 创建集群描述符
     * 2. 构建PackagedProgram
     * 3. 生成JobGraph
     * 4. 提交作业
     * 5. 返回作业信息（不阻塞等待）
     *
     * @param jarParam      JAR执行参数
     * @param configuration Flink配置
     * @return JobDeployResult 包含作业ID、集群信息、Web界面URL等
     */
    protected JobDeployRespDto submitJarTemplate(DeployJarParam jarParam, Configuration configuration) {
        JobDeployRespDto respDto = new JobDeployRespDto();
        try (StandaloneClusterDescriptor clusterDescriptor = new StandaloneClusterDescriptor(configuration)) {
            log.info("连接到远程集群");
            ClusterClientProvider<StandaloneClusterId> clusterClientProvider =
                    clusterDescriptor.retrieve(StandaloneClusterId.getInstance());
            log.info("获取ClusterClient实例");
            ClusterClient<StandaloneClusterId> clusterClient = clusterClientProvider.getClusterClient();
            log.info("构建PackagedProgram");
            // 如果未设置 entryPointClass，则从JAR manifest中读取
            PackagedProgram program = PackagedProgram.newBuilder()
                    .setJarFile(new File(jarParam.getJarFile()))
                    .setEntryPointClassName(jarParam.getEntryPointClassName())
                    .setArguments(jarParam.getArgument())
                    .setSavepointRestoreSettings(SavepointRestoreSettings.none())
                    .build();
            log.info("生成JobGraph");
            configuration.set(PipelineOptions.NAME, jarParam.getJobName());
            JobGraph jobGraph = PackagedProgramUtils.createJobGraph(
                    program, configuration, 1, false);
            log.info("提交作业到集群");
            CompletableFuture<JobID> jobIdFuture =
                    clusterClient.submitJob(jobGraph);
            JobID jobId = jobIdFuture.get();
            log.info("作业已成功提交，作业ID: {}", jobId);
            log.info("可以通过以下URL查看作业状态: {}/#/job/{}", clusterClient.getWebInterfaceURL(), jobId);
            respDto.setJobId(jobId.toString());
            respDto.setConfig(clusterClient.getFlinkConfiguration().toMap());
            respDto.setSubmitStatus(true);
            respDto.setSubmitTime(LocalDateTimeUtil.now());
            respDto.setDeployMode(jarParam.getDeployMode());
            respDto.setWebInterfaceUrl(clusterClient.getWebInterfaceURL());
            return respDto;
        } catch (Exception e) {
            log.error("连接或提交作业时出错", e);
            throw ServiceExceptionUtil.exception(new ErrorCode(9999, "连接或提交作业时出错: {}"), e.getMessage());
        }
    }


    /**
     * 模板方法：执行SQL语句
     * <p>
     * 封装了SQL执行流程：
     * 1. 创建StreamExecutionEnvironment（由子类实现）
     * 2. 创建StreamTableEnvironment
     * 3. 解析和执行SQL语句
     * 4. 处理ModifyOperation和其他操作类型
     *
     * @param sqlParam    SQL执行参数
     * @param environment StreamExecutionEnvironment（由子类提供）
     */
    protected JobDeployRespDto executeSqlTemplate(DeploySqlParam sqlParam, StreamExecutionEnvironment environment) {
        try {
            StreamTableEnvironment stbEnv = StreamTableEnvironment.create(environment);
            StreamStatementSet statementSet = stbEnv.createStatementSet();
            TableEnvironmentImpl tbEnv = (TableEnvironmentImpl) stbEnv;
            boolean useStatementSet = false;
            String[] statements = SqlUtil.getStatements(sqlParam.getSql());
            for (String statement : statements) {
                Operation operation = tbEnv.getParser().parse(statement).get(0);
                if (operation instanceof ModifyOperation) {
                    statementSet.addInsertSql(statement);
                    useStatementSet = true;
                } else if (operation instanceof ShowOperation || operation instanceof PlannerQueryOperation) {
                    throw ServiceExceptionUtil.exception(new ErrorCode(9999, "不支持的Show SQL 或 SELECT SQL 类型{}"), operation.getClass().getName());
                } else {
                    tbEnv.executeSql(statement);
                }
            }

            if (useStatementSet) {
                statementSet.attachAsDataStream();
            }
            JobClient jobClient = environment.executeAsync(sqlParam.getJobName());
            JobDeployRespDto respDto = new JobDeployRespDto();
            Map<String, String> config = ((Configuration) environment.getConfiguration()).toMap();
            String address = environment.getConfiguration().get(RestOptions.ADDRESS);
            if (address == null) {
                address = "localhost";
            }
            config.put(RestOptions.ADDRESS.key(), address);
            respDto.setConfig(config);
            respDto.setJobId(jobClient.getJobID().toString());
            respDto.setSubmitStatus(true);
            respDto.setSubmitTime(LocalDateTimeUtil.now());
            respDto.setWebInterfaceUrl(address + ":" + environment.getConfiguration().get(RestOptions.PORT));
            log.info("sql作业已提交，作业ID: {}", jobClient.getJobID());
            return respDto;
        } catch (Exception e) {
            throw new RuntimeException("执行Flink SQL作业时发生错误: " + e.getMessage(), e);
        }
    }


    /**
     * 通用参数验证方法
     * <p>
     * 类型安全的参数验证和转换，避免重复的类型检查代码
     *
     * @param param        原始参数
     * @param expectedType 期望的类型
     * @param <T>          期望的类型参数
     * @return 转换后的参数
     */
    protected <T extends DeployParam> T validateParam(DeployParam param, Class<T> expectedType) {
        if (!expectedType.isInstance(param)) {
            throw ServiceExceptionUtil.exception(
                    new ErrorCode(9999, "不支持此类型执行参数{}，期望类型: {}"),
                    param.getClass().getName(),
                    expectedType.getName()
            );
        }
        return expectedType.cast(param);
    }

    /**
     * JAR执行参数抽象接口
     * 用于封装JAR作业执行所需的参数
     */
    public interface DeployJarParam {
        String getJarFile();

        String getEntryPointClassName();

        String[] getArgument();

        String getDeployMode();

        String getJobName();
    }

    /**
     * SQL执行参数抽象接口
     * 用于封装SQL作业执行所需的参数
     */
    public interface DeploySqlParam {
        String getSql();

        String getDeployMode();

        String getJobName();
    }
}
