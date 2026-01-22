package cn.iocoder.yudao.module.flink.common.api;

import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author yzh
 */
@FeignClient("flink-119-service")
public interface Flink119Api extends FlinkApi {
  @Override
  default String getVersion() {

    return "1.19";
  }
}
