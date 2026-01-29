package cn.iocoder.yudao.module.flink.deploy;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.flink.common.dto.JobDeployRespDto;
import cn.iocoder.yudao.module.flink.deploy.base.FlinkApplicationExecutor;
import cn.iocoder.yudao.module.flink.deploy.param.DeployLocalDataIngestionParam;
import org.junit.jupiter.api.Test;

/**
 * FlinkApplicationExecutor 测试类
 *
 * <p>测试参数说明（对应 main 方法 args 参数）： args[0]: JSON 格式的部署参数 args[1]: 作业类型（DATA_INGESTION） args[2]: Nacos
 * 地址 args[3]: Nacos 命名空间
 *
 * @author yzh
 */
public class FlinkApplicationExecutorTest {

  /**
   * 测试数据摄入功能（带配置）
   *
   * <p>该测试方法验证 Flink 本地执行器能否正确执行 MySQL 到 Doris 的数据同步作业。 测试参数来源于前端传递的 JSON 配置。
   *
   * <p>测试内容： - MySQL 源表：127.0.0.1:3306 上的 ruoyi-vue-pro-2 数据库 - Doris 目标：127.0.0.1:8035
   *
   * @see FlinkJobLocalDeployerImpl#(DeployLocalDataIngestionParam)
   */
  @Test
  public void testDataIngestionWithConfig() {
    // 构建部署参数
    DeployLocalDataIngestionParam param = buildDataIngestionParam();

    FlinkApplicationExecutor.main(
        new String[] {JSONUtil.toJsonStr(param), "DATA_INGESTION", "127.0.0.1:8848", "public"});
  }

  /**
   * 测试数据摄入功能（无配置，使用默认配置）
   *
   * <p>该测试方法验证 Flink 本地执行器能否使用默认配置执行数据同步作业。
   */
  @Test
  public void testDataIngestionWithoutConfig() {
    // 构建部署参数（不设置 configuration，使用默认配置）
    String content =
        "source:\n"
            + "  type: mysql\n"
            + "  name: MySQL Source\n"
            + "  hostname: 127.0.0.1\n"
            + "  port: 3306\n"
            + "  username: root\n"
            + "  password: mysql_dsEsnN\n"
            + "  tables: ruoyi-vue-pro-2.\\.*\n"
            + "  jdbc.properties.allowPublicKeyRetrieval: true\n"
            + "\n"
            + "sink:\n"
            + "  type: doris\n"
            + "  name: Doris Sink\n"
            + "  fenodes: 127.0.0.1:8035\n"
            + "  benodes: 127.0.0.1:8041\n"
            + "  username: root\n"
            + "  password: admin\n"
            + "  table.create.properties.replication_num: 1\n"
            + "\n"
            + "pipeline:\n"
            + "  name: MySQL to Doris Pipeline\n";

    DeployLocalDataIngestionParam param = new DeployLocalDataIngestionParam();
    param.setContent(content);
    param.setJobName("MySQL to Doris Pipeline");
    param.setClusterId(4L);
    param.setFileId(2L);
    // 不设置 configuration，使用默认配置

    // 创建本地执行器并部署
    FlinkJobLocalDeployerImpl deployer = new FlinkJobLocalDeployerImpl();
    JobDeployRespDto respDto = deployer.deployDataIngestion(param);

    System.out.println("部署结果: " + respDto);
  }

  /**
   * 构建数据摄入参数（模拟 main 方法 args 参数）
   *
   * <p>main 方法参数示例： args[0] = {"content":"...", "jobName":"...", "clusterId":4, "fileId":2} args[1]
   * = "DATA_INGESTION" args[2] = "127.0.0.1:8848" args[3] = "public"
   */
  private DeployLocalDataIngestionParam buildDataIngestionParam() {
    // YAML 管道配置（MySQL -> Doris）
    String content =
        "source:\n"
            + "  type: mysql\n"
            + "  name: MySQL Source\n"
            + "  hostname: 127.0.0.1\n"
            + "  port: 3306\n"
            + "  username: root\n"
            + "  password: mysql_dsEsnN\n"
            + "  tables: ruoyi-vue-pro-2.\\.*\n"
            + "  jdbc.properties.allowPublicKeyRetrieval: true\n"
            + "\n"
            + "sink:\n"
            + "  type: doris\n"
            + "  name: Doris Sink\n"
            + "  fenodes: 127.0.0.1:8035\n"
            + "  benodes: 127.0.0.1:8041\n"
            + "  username: root\n"
            + "  password: admin\n"
            + "  table.create.properties.replication_num: 1\n"
            + "\n"
            + "pipeline:\n"
            + "  name: MySQL to Doris Pipeline\n";

    // 构建配置

    // 构建参数对象
    DeployLocalDataIngestionParam param = new DeployLocalDataIngestionParam();
    param.setContent(content);
    param.setJobName("MySQL to Doris Pipeline");
    param.setClusterId(4L);
    param.setClusterName("Flink Cluster");
    param.setFileId(2L);

    return param;
  }

  /**
   * 测试 main 方法参数解析
   *
   * <p>模拟 main 方法接收的参数： args[0] = JSON 格式的参数 args[1] = 作业类型 args[2] = Nacos 地址 args[3] = Nacos 命名空间
   */
  @Test
  public void testMainArgsParsing() {
    // 模拟 main 方法接收的参数
    String[] args = new String[4];

    // args[0]: JSON 格式的部署参数
    String jsonParam =
        "{\n"
            + "  \"content\": \"source:\\n  type: mysql\\n  hostname: 127.0.0.1\\n  port: 3306\\n\",\n"
            + "  \"jobName\": \"MySQL to Doris Pipeline\",\n"
            + "  \"clusterId\": 4,\n"
            + "  \"fileId\": 2\n"
            + "}";
    args[0] = jsonParam;

    // args[1]: 作业类型
    args[1] = "DATA_INGESTION";

    // args[2]: Nacos 地址
    args[2] = "127.0.0.1:8848";

    // args[3]: Nacos 命名空间
    args[3] = "public";

    // 验证参数解析
    System.out.println("参数数量: " + args.length);
    System.out.println("JSON参数: " + args[0]);
    System.out.println("作业类型: " + args[1]);
    System.out.println("Nacos地址: " + args[2]);
    System.out.println("命名空间: " + args[3]);
  }
}
