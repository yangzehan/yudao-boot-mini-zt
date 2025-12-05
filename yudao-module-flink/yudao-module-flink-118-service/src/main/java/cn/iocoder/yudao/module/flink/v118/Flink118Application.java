package cn.iocoder.yudao.module.flink.v118;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * Flink 1.18服务启动类
 *
 * @author yzh
 * @since 2025-11-27
 */
@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan("cn.iocoder.yudao.module.*")
public class Flink118Application {

    public static void main(String[] args) {
        SpringApplication.run(Flink118Application.class, args);
        System.out.println("\n  ______   ______   ______   ______   ______   ______   ______   ______  \n" +
                "/\\  == \\ /\\  __ \\ /\\  __ \\ /\\  __ \\ /\\  __ \\ /\\  __ \\ /\\  == \\ /\\  ___\\ \n" +
                "\\ \\  __< \\ \\  __ \\ \\ \\  __ \\ \\ \\/\\ \\ \\ \\  __ \\ \\ \\  __ \\ \\  __< \\ \\  __\\ \n" +
                " \\ \\_____\\ \\_\\ \\_\\ \\ \\_\\ \\_\\ \\_____\\ \\_\\ \\_\\ \\ \\_\\ \\_\\ \\_____\\ \\_____\\\n" +
                "  \\/_____/ \\/_/\\/_/  \\/_/\\/_/  \\/_____/ \\/_/\\/_/  \\/_/\\/_/ \\/_____/ \\/_____/\n" +
                "                                                                              \n" +
                "  :: Flink V2 Service ::                (v1.18.1)  \n" +
                " ----------------------------------------------------------------------------\n" +
                "  Flink 1.18版本服务启动成功！\n" +
                "  服务地址: http://localhost:8085\n" +
                "  管理端点: http://localhost:8085/actuator\n" +
                " ----------------------------------------------------------------------------\n");
    }

}
