package cn.iocoder.yudao.module.flink.common.api;


import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("flink-117-service")
public interface Flink117Api extends FlinkApi {
}
