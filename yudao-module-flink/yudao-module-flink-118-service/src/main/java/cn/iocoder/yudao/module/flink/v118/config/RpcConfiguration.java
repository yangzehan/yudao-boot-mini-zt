package cn.iocoder.yudao.module.flink.v118.config;

import cn.iocoder.yudao.module.datastudio.api.job.DataJobApi;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@EnableFeignClients(clients = {DataJobApi.class})
@Configuration(value = "Flink118RpcConfig", proxyBeanMethods = false)
public class RpcConfiguration {}
