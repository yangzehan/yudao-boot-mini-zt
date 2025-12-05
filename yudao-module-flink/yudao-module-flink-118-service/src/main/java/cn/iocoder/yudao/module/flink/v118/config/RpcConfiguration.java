package cn.iocoder.yudao.module.flink.v118.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@EnableFeignClients()
@Configuration(value = "Flink118RpcConfig", proxyBeanMethods = false)
public class RpcConfiguration {
}
