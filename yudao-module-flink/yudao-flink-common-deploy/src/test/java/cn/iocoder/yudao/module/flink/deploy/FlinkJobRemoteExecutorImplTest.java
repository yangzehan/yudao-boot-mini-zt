package cn.iocoder.yudao.module.flink.deploy;

import cn.iocoder.yudao.module.flink.deploy.deployer.FlinkJobRemoteDeployerImpl;
import cn.iocoder.yudao.module.flink.deploy.param.DeployRemoteJarParam;
import cn.iocoder.yudao.module.flink.deploy.param.DeployRemoteSqlParam;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestOptions;
import org.junit.jupiter.api.Test;

class FlinkJobRemoteExecutorImplTest {
  /**
   * 测试远程执行Flink SQL的功能
   *
   * <p>该测试方法验证Flink远程执行器能否通过REST API正确提交并执行SQL语句。
   * 主要包括创建数据源表(datagen_source)、创建输出表(print_sink)以及执行数据插入操作。
   *
   * <p>测试流程： 1. 构造远程SQL执行参数，包括连接配置和SQL语句 2. 设置远程Flink集群的REST服务地址和端口 3. SQL语句包含创建源表、创建打印输出表和数据插入操作
   * 4. 创建远程执行器实例并执行SQL
   *
   * @see FlinkJobRemoteDeployerImpl#executeSql(DeployRemoteSqlParam)
   */
  @Test
  void executeSql() {
    // 构造远程SQL执行参数对象
    DeployRemoteSqlParam executeParam = new DeployRemoteSqlParam();

    // 配置远程Flink集群的REST服务地址和端口
    Configuration configuration = new Configuration();
    configuration.set(RestOptions.ADDRESS, "localhost");
    configuration.set(RestOptions.PORT, 8081);
    executeParam.setConfiguration(configuration);

    // 设置要执行的SQL语句，包括创建源表、目标表和插入数据操作
    executeParam.setSql(
        "-- 创建一个使用 datagen 连接器的数据表\n"
            + "CREATE TABLE datagen_source (\n"
            + "  id BIGINT,\n"
            + "  name STRING,\n"
            + "  age INT,\n"
            + "  address STRING,\n"
            + "  proctime AS PROCTIME()\n"
            + ") WITH (\n"
            + "  'connector' = 'datagen',\n"
            + "  'rows-per-second' = '10',\n"
            + "  'fields.id.kind' = 'sequence',\n"
            + "  'fields.id.start' = '1',\n"
            + "  'fields.id.end' = '1000',\n"
            + "  'fields.name.length' = '10',\n"
            + "  'fields.age.min' = '18',\n"
            + "  'fields.age.max' = '80',\n"
            + "  'fields.address.length' = '20'\n"
            + ");\n"
            + "\n"
            + "-- 创建一个打印连接器的表用于输出结果\n"
            + "CREATE TABLE print_sink (\n"
            + "  id BIGINT,\n"
            + "  name STRING,\n"
            + "  age INT,\n"
            + "  address STRING\n"
            + ") WITH (\n"
            + "  'connector' = 'print'\n"
            + ");\n"
            + "\n"
            + "-- 将数据从源表插入到打印表中\n"
            + "INSERT INTO print_sink SELECT id, name, age, address FROM datagen_source;");

    // 创建远程执行器实例并执行SQL
    new FlinkJobRemoteDeployerImpl().deploySql(executeParam);
  }

  /**
   * 测试远程执行JAR包的功能
   *
   * <p>该测试方法验证Flink远程执行器能否通过REST API正确提交并执行指定的JAR文件。 主要包括设置远程Flink集群连接配置、指定JAR文件路径等操作。
   *
   * <p>测试流程： 1. 创建Flink远程执行器实例 2. 构造远程执行参数，包括连接配置和JAR文件信息 3. 设置Flink集群的REST服务地址和端口 4. 指定要执行的JAR文件路径
   * 5. 调用执行器向远程集群提交JAR任务
   *
   * @see FlinkJobRemoteDeployerImpl#executeJar(DeployRemoteJarParam)
   */
  @Test
  void executeJar() {
    // 创建远程Flink作业执行器实例
    FlinkJobRemoteDeployerImpl executor = new FlinkJobRemoteDeployerImpl();

    // 构造远程执行参数对象
    DeployRemoteJarParam executeParam = new DeployRemoteJarParam();

    // 配置远程Flink集群的REST服务地址和端口
    Configuration configuration = new Configuration();
    configuration.set(RestOptions.ADDRESS, "localhost");
    configuration.set(RestOptions.PORT, 8081);

    // 设置要执行的JAR文件路径
    executeParam.setJarFile(
        "F:\\Users\\yzh\\IdeaProjects\\zt\\yudao-boot-mini-zt\\yudao-module-flink\\yudao-flink-common-executor\\src\\test\\resources\\WordCount.jar");

    // 设置执行参数
    executeParam.setConfiguration(configuration);
    executeParam.setArgument(new String[0]);
    executeParam.setEntryPointClassName(null);

    // 执行远程JAR包提交
    executor.deployJar(executeParam, true);
  }
}
