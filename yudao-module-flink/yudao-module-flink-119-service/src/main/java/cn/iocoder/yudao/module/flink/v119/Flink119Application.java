package cn.iocoder.yudao.module.flink.v119;

import cn.iocoder.yudao.framework.common.util.spring.SpringUtils;
import cn.iocoder.yudao.module.flink.deploy.constant.NacosConstant;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Flink 1.18服务启动类
 *
 * @author yzh
 * @since 2025-11-27
 */
@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan("cn.iocoder.yudao.module.*")
@EnableAsync
@EnableScheduling
public class Flink119Application {

  public static void main(String[] args) {
    SpringApplication.run(Flink119Application.class, args);
    System.out.println(
        "\n  ______   ______   ______   ______   ______   ______   ______   ______  \n"
            + "/\\  == \\ /\\  __ \\ /\\  __ \\ /\\  __ \\ /\\  __ \\ /\\  __ \\ /\\  == \\ /\\  ___\\ \n"
            + "\\ \\  __< \\ \\  __ \\ \\ \\  __ \\ \\ \\/\\ \\ \\ \\  __ \\ \\ \\  __ \\ \\  __< \\ \\  __\\ \n"
            + " \\ \\_____\\ \\_\\ \\_\\ \\ \\_\\ \\_\\ \\_____\\ \\_\\ \\_\\ \\ \\_\\ \\_\\ \\_____\\ \\_____\\\n"
            + "  \\/_____/ \\/_/\\/_/  \\/_/\\/_/  \\/_____/ \\/_/\\/_/  \\/_/\\/_/ \\/_____/ \\/_____/\n"
            + "                                                                              \n"
            + "  :: Flink V2 Service ::                (v1.18.1)  \n"
            + " ----------------------------------------------------------------------------\n"
            + "  Flink 1.18版本服务启动成功！\n"
            + "  服务地址: http://localhost:8085\n"
            + "  管理端点: http://localhost:8085/actuator\n"
            + " ----------------------------------------------------------------------------\n");
    NacosConstant.setDiscoveryNamespace(
        SpringUtils.getProperty("spring.cloud.nacos.discovery.namespace"));
    NacosConstant.setDiscoveryServerAddr(
        SpringUtils.getProperty("spring.cloud.nacos.discovery.server-addr"));
  }
}
