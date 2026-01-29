package cn.iocoder.yudao.module.flink.deploy.constant;

/**
 * @author yzh
 */
public class NacosConstant {
  private static String DISCOVERY_SERVER_ADDR = "";
  private static String DISCOVERY_NAMESPACE = "";

  public static String getDiscoveryServerAddr() {
    return DISCOVERY_SERVER_ADDR;
  }

  public static void setDiscoveryServerAddr(String discoveryServerAddr) {
    DISCOVERY_SERVER_ADDR = discoveryServerAddr;
  }

  public static String getDiscoveryNamespace() {
    return DISCOVERY_NAMESPACE;
  }

  public static void setDiscoveryNamespace(String discoveryNamespace) {
    DISCOVERY_NAMESPACE = discoveryNamespace;
  }
}
