package cn.iocoder.yudao.module.flink.common.api;


import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("flink-118-service")
public interface
Flink118Api extends FlinkApi {
    @Override
    default String getVersion() {
        return "1.18";
    }


}
