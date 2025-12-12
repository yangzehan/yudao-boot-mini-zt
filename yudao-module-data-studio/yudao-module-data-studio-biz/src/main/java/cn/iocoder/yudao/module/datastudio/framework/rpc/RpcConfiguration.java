package cn.iocoder.yudao.module.datastudio.framework.rpc;

import cn.iocoder.yudao.module.flink.common.api.Flink118Api;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration(value = "DataStudioConfiguration",proxyBeanMethods = false)
@EnableFeignClients(clients = {Flink118Api.class})
public class RpcConfiguration {

}
