package cn.iocoder.yudao.module.datastudio.framework.security;

import cn.iocoder.yudao.framework.security.config.AuthorizeRequestsCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

/** Infra 模块的 Security 配置 */
@Configuration(proxyBeanMethods = false, value = "dataStudioSecurityConfiguration")
public class SecurityConfiguration {

  @Value("${spring.boot.admin.context-path:''}")
  private String adminSeverContextPath;

  @Bean("dataStudioAuthorizeRequestsCustomizer")
  public AuthorizeRequestsCustomizer authorizeRequestsCustomizer() {
    return new AuthorizeRequestsCustomizer() {

      @Override
      public void customize(
          AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry
              registry) {
        registry.requestMatchers("/data-studio/job/**").permitAll();
      }
    };
  }
}
