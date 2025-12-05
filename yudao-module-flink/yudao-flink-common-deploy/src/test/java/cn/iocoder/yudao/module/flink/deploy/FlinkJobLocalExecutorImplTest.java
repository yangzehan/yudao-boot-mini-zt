package cn.iocoder.yudao.module.flink.deploy;

import cn.iocoder.yudao.module.flink.common.dto.JobDeployRespDto;
import cn.iocoder.yudao.module.flink.deploy.param.DeployLocalJarParam;
import cn.iocoder.yudao.module.flink.deploy.param.DeployLocalSqlParam;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestOptions;
import org.junit.jupiter.api.Test;

class FlinkJobLocalExecutorImplTest {

    /**
     * 测试执行Flink SQL的功能
     * <p>
     * 该测试方法验证Flink本地执行器能否正确执行SQL语句。
     * 主要包括创建数据源表(datagen_source)、创建输出表(print_sink)以及执行数据插入操作。
     * <p>
     * 测试流程：
     * 1. 创建Flink本地执行器实例
     * 2. 构造SQL执行参数，包括配置信息和SQL语句
     * 3. SQL语句包含创建源表、创建打印输出表和数据插入操作
     * 4. 调用执行器执行SQL并获取结果
     * 5. 打印执行结果
     *
     * @see FlinkJobLocalDeployerImpl#executeSql(DeployLocalSqlParam)
     */
    @Test
    void executeSql() {
        // 创建本地Flink SQL执行器实例
        FlinkJobLocalDeployerImpl executor = new FlinkJobLocalDeployerImpl();

        // 构造SQL执行参数对象
        DeployLocalSqlParam executeParam = new DeployLocalSqlParam();
        Configuration configuration = new Configuration();
        executeParam.setConfiguration(configuration);

        // 设置要执行的SQL语句，包括创建源表、目标表和插入数据操作
        executeParam.setSql(
                "CREATE TABLE datagen_source (\n" +
                        "  id BIGINT,\n" +
                        "  name STRING,\n" +
                        "  age INT,\n" +
                        "  address STRING,\n" +
                        "  proctime AS PROCTIME()\n" +
                        ") WITH (\n" +
                        "  'connector' = 'datagen',\n" +
                        "  'rows-per-second' = '10',\n" +
                        "  'fields.id.kind' = 'sequence',\n" +
                        "  'fields.id.start' = '1',\n" +
                        "  'fields.id.end' = '1000',\n" +
                        "  'fields.name.length' = '10',\n" +
                        "  'fields.age.min' = '18',\n" +
                        "  'fields.age.max' = '80',\n" +
                        "  'fields.address.length' = '20'\n" +
                        ");\n" +
                        "\n" +
                        "-- 创建一个打印连接器的表用于输出结果\n" +
                        "CREATE TABLE print_sink (\n" +
                        "  id BIGINT,\n" +
                        "  name STRING,\n" +
                        "  age INT,\n" +
                        "  address STRING\n" +
                        ") WITH (\n" +
                        "  'connector' = 'print'\n" +
                        ");\n" +
                        "\n" +
                        "-- 将数据从源表插入到打印表中\n" +
                        "INSERT INTO print_sink SELECT id, name, age, address FROM datagen_source;"
        );

        // 执行SQL并获取结果
        JobDeployRespDto respDto = executor.deploySql(executeParam);
        System.out.println(respDto);
    }


    /**
     * 测试执行JAR包的功能
     * <p>
     * 该测试方法验证Flink本地执行器能否正确执行指定的JAR文件。
     * 主要包括设置执行配置、指定JAR文件路径等操作。
     *
     * @see FlinkJobLocalDeployerImpl#executeJar(DeployLocalJarParam)
     */
    @Test
    void executeJar() {
        // 创建本地Flink作业执行器实例
        FlinkJobLocalDeployerImpl flinkJobLocalExecutor = new FlinkJobLocalDeployerImpl();

        // 构造执行参数对象
        DeployLocalJarParam deployLocalJarParam = new DeployLocalJarParam();

        // 配置Flink REST服务地址和端口
        Configuration configuration = new Configuration();
        configuration.set(RestOptions.ADDRESS, "localhost");
        configuration.set(RestOptions.PORT, 8081);
        deployLocalJarParam.setConfiguration(configuration);

        // 设置要执行的JAR文件路径
        deployLocalJarParam.setJarFile("F:\\Users\\yzh\\IdeaProjects\\zt\\yudao-boot-mini-zt\\yudao-module-flink\\yudao-flink-common-executor\\src\\test\\resources\\WordCount.jar");

        // 设置程序参数和入口类名
        deployLocalJarParam.setArgument(new String[0]);
        deployLocalJarParam.setEntryPointClassName(null);

        // 执行JAR包
        flinkJobLocalExecutor.deployJar(deployLocalJarParam);
    }

}