package cn.iocoder.yudao.module.flink.v118.api;

import cn.iocoder.yudao.module.flink.common.dto.FlinkConfig;
import cn.iocoder.yudao.module.flink.common.dto.JobDeploySqlReqDto;
import org.apache.flink.configuration.RestOptions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.HashMap;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class Flink118ApiImplTest {
    @Resource
    private Flink118ApiImpl flink118Api;

    /**
     * 测试提交Flink SQL作业的功能
     * 该测试构造了一个包含数据生成源表和打印输出表的SQL作业，
     * 并配置了远程Flink集群的连接信息进行作业提交
     */
    @Test
    void submitRemoteSql() {
        // 构造SQL作业请求参数
        JobDeploySqlReqDto request = JobDeploySqlReqDto.builder().build();
        // 设置包含源表定义、目标表定义及数据插入语句的完整SQL脚本
        request.setSql("-- 创建一个使用 datagen 连接器的数据表\n" +
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
                "INSERT INTO print_sink SELECT id, name, age, address FROM datagen_source;");

        // 配置Flink作业运行参数
        FlinkConfig flinkConfig = new FlinkConfig();
        flinkConfig.setDeployMode("remote");
        HashMap<String, String> extendedConfig = new HashMap<>();
        // 设置远程Flink集群的地址和端口
        extendedConfig.put(RestOptions.ADDRESS.key(), "127.0.0.1");
        extendedConfig.put(RestOptions.PORT.key(), "8081");
        flinkConfig.setExtendedConfig(extendedConfig);
        request.setFlinkConfig(flinkConfig);

        // 执行SQL作业提交
        flink118Api.deploySql(request);

    }

    /**
     * 测试提交Flink SQL作业的功能
     * 该测试构造了一个包含数据生成源表和打印输出表的SQL作业，
     * 并配置了远程Flink集群的连接信息进行作业提交
     */
    @Test
    void submitLocalSql() {
        // 构造SQL作业请求参数
        JobDeploySqlReqDto request = JobDeploySqlReqDto.builder().build();
        // 设置包含源表定义、目标表定义及数据插入语句的完整SQL脚本
        request.setSql("-- 创建一个使用 datagen 连接器的数据表\n" +
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
                "INSERT INTO print_sink SELECT id, name, age, address FROM datagen_source;");

        // 配置Flink作业运行参数
        FlinkConfig flinkConfig = new FlinkConfig();
        flinkConfig.setDeployMode("local");
        HashMap<String, String> extendedConfig = new HashMap<>();
        flinkConfig.setExtendedConfig(extendedConfig);
        request.setFlinkConfig(flinkConfig);

        // 执行SQL作业提交
        flink118Api.deploySql(request);

    }
}