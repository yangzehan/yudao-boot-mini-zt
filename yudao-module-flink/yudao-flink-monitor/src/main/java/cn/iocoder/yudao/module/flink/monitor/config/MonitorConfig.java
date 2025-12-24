package cn.iocoder.yudao.module.flink.monitor.config;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.runtime.rest.RestClient;
import org.apache.flink.util.ConfigurationException;
import org.apache.flink.util.concurrent.ExecutorThreadFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author yzh
 */
@Configuration
public class MonitorConfig {
  // 添加默认值
  @Value("${flink.rest.rest.connection-timeout:2000}")
  private Long timeout;

  @Value("${flink.rest.idleness-timeout:10000}")
  private Long idlenessTimeout;

  @Value("${flink.rest.rest.max-content-length:104857600}")
  private Integer maxContentLength;

  @Bean
  public RestClient restClient(
      org.apache.flink.configuration.Configuration clientConfiguration, Executor monitorExecutor) {

    try {
      return new RestClient(clientConfiguration, monitorExecutor);
    } catch (ConfigurationException e) {
      throw new RuntimeException(e);
    }
  }

  @Bean
  public org.apache.flink.configuration.Configuration clientConfiguration() {
    org.apache.flink.configuration.Configuration configuration =
        new org.apache.flink.configuration.Configuration();
    configuration.set(RestOptions.CONNECTION_TIMEOUT, timeout);
    configuration.set(RestOptions.IDLENESS_TIMEOUT, idlenessTimeout);
    configuration.set(RestOptions.CLIENT_MAX_CONTENT_LENGTH, maxContentLength);
    return configuration;
  }

  @Bean
  public Executor monitorExecutor() {
    return new ThreadPoolExecutor(
        8,
        20,
        60L,
        TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(),
        new ExecutorThreadFactory("flink-rest-io"));
  }
}
