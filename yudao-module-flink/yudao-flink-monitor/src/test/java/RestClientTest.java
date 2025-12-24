import cn.iocoder.yudao.module.flink.monitor.config.MonitorConfig;
import javax.annotation.Resource;
import org.apache.flink.runtime.rest.RestClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author yzh
 */
@SpringBootTest(classes = MonitorConfig.class)
public class RestClientTest {
  @Resource RestClient restClient;

  @Test
  public void test() {
    System.out.println(restClient);
  }
}
