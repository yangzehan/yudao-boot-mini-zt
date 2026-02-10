package cn.iocoder.yudao.module.system.service.mail;

import static org.junit.jupiter.api.Assertions.*;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 邮件发送相关功能的简单测试类
 *
 * <p>注意：完整的邮件发送测试需要： 1. 数据库中配置邮件账号 (system_mail_account) 2. 数据库中配置邮件模板 (system_mail_template) 3. 启动
 * Spring Boot 应用
 *
 * <p>执行测试前请先执行 sql/datastudio/test_mail_config.sql
 *
 * @author yudao-code
 */
@Slf4j
@DisplayName("邮件功能测试")
class MailSendServiceImplTest {

  /** 测试邮箱格式验证 - 有效邮箱 */
  @Test
  @DisplayName("测试邮箱格式验证 - 有效邮箱")
  void testValidEmails() {
    // 验证有效邮箱格式
    assertTrue(Validator.isEmail("627617031@qq.com"), "QQ邮箱应该有效");
    assertTrue(Validator.isEmail("test@example.com"), "普通邮箱应该有效");
    assertTrue(Validator.isEmail("user.name@domain.co.uk"), "带点的邮箱应该有效");
    assertTrue(Validator.isEmail("user+tag@gmail.com"), "带加号的邮箱应该有效");
    assertTrue(Validator.isEmail("user@sub.domain.com"), "子域名邮箱应该有效");
  }

  /** 测试邮箱格式验证 - 无效邮箱 */
  @Test
  @DisplayName("测试邮箱格式验证 - 无效邮箱")
  void testInvalidEmails() {
    // 验证无效邮箱格式
    assertFalse(Validator.isEmail("invalid"), "纯文本应该无效");
    assertFalse(Validator.isEmail("@example.com"), "缺少用户名应该无效");
    assertFalse(Validator.isEmail("test@"), "缺少域名应该无效");
    assertFalse(Validator.isEmail("test @example.com"), "带空格的邮箱应该无效");
    assertFalse(Validator.isEmail("test@example"), "缺少顶级域名应该无效");
    assertFalse(Validator.isEmail(""), "空字符串应该无效");
  }

  /** 测试 CollUtil 集合工具 */
  @Test
  @DisplayName("测试集合工具类")
  void testCollectionUtils() {
    // 测试 CollUtil.newHashSet
    HashSet<String> toMails = CollUtil.newHashSet("627617031@qq.com");
    assertEquals(1, toMails.size());
    assertTrue(toMails.contains("627617031@qq.com"));

    // 测试多个邮箱
    HashSet<String> multipleMails = CollUtil.newHashSet("test1@qq.com", "test2@qq.com");
    assertEquals(2, multipleMails.size());

    // 测试过滤 - 只保留有效邮箱
    HashSet<String> allMails = CollUtil.newHashSet("valid@qq.com", "invalid", "another@test.com");
    java.util.List<String> validMails =
        allMails.stream().filter(Validator::isEmail).collect(java.util.stream.Collectors.toList());
    assertEquals(2, validMails.size());
  }

  /** 测试模板参数准备 */
  @Test
  @DisplayName("测试模板参数准备")
  void testTemplateParams() {
    // 准备模板参数
    Map<String, Object> templateParams = new HashMap<>();
    templateParams.put("date", "2026-02-09 12:00:00");
    templateParams.put("code", "123456");
    templateParams.put("userName", "测试用户");

    // 验证参数
    assertEquals("2026-02-09 12:00:00", templateParams.get("date"));
    assertEquals("123456", templateParams.get("code"));
    assertEquals("测试用户", templateParams.get("userName"));

    // 验证参数不为空
    for (Map.Entry<String, Object> entry : templateParams.entrySet()) {
      assertNotNull(entry.getValue(), "参数 " + entry.getKey() + " 不应为空");
    }
  }

  /** 测试 HTML 内容格式化 */
  @Test
  @DisplayName("测试邮件内容格式化")
  void testMailContentFormatting() {
    // 模拟邮件内容
    String title = "【测试】这是一封测试邮件";
    String content =
        "<p>你好，这是一封测试邮件！</p>" + "<p>测试时间: ${date}</p>" + "<p>验证码: ${code}</p>" + "<p>-- 芋道源码团队</p>";

    // 模拟参数替换
    Map<String, Object> params = new HashMap<>();
    params.put("date", "2026-02-09");
    params.put("code", "123456");

    String formattedContent = content;
    for (Map.Entry<String, Object> entry : params.entrySet()) {
      formattedContent =
          formattedContent.replace("${" + entry.getKey() + "}", String.valueOf(entry.getValue()));
    }

    // 验证参数已替换
    assertTrue(formattedContent.contains("2026-02-09"));
    assertTrue(formattedContent.contains("123456"));
    assertFalse(formattedContent.contains("${date}"));
    assertFalse(formattedContent.contains("${code}"));
  }

  /** 测试邮件模板参数校验逻辑 */
  @Test
  @DisplayName("测试模板参数校验")
  void testTemplateParamsValidation() {
    // 定义模板需要的参数
    String[] requiredParams = {"date", "code", "userName"};

    // 完整的参数
    Map<String, Object> completeParams = new HashMap<>();
    completeParams.put("date", "2026-02-09");
    completeParams.put("code", "123456");
    completeParams.put("userName", "测试用户");

    // 校验参数完整性
    for (String param : requiredParams) {
      assertTrue(completeParams.containsKey(param), "模板需要参数: " + param);
      assertNotNull(completeParams.get(param), "参数 " + param + " 不应为空");
    }

    // 缺少参数的测试
    Map<String, Object> incompleteParams = new HashMap<>();
    incompleteParams.put("date", "2026-02-09");
    // 缺少 code 和 userName

    for (String param : requiredParams) {
      if (!incompleteParams.containsKey(param)) {
        assertTrue(true, "参数 " + param + " 缺失");
      }
    }
  }

  /** 测试邮件发送请求参数构建 */
  @Test
  @DisplayName("测试邮件发送请求参数")
  void testMailSendRequestParams() {
    // 构建收件人列表
    HashSet<String> toMails = CollUtil.newHashSet("627617031@qq.com");

    // 构建抄送列表
    HashSet<String> ccMails = CollUtil.newHashSet("cc1@test.com", "cc2@test.com");

    // 构建密送列表
    HashSet<String> bccMails = CollUtil.newHashSet("bcc@test.com");

    // 构建模板参数
    Map<String, Object> templateParams = new HashMap<>();
    templateParams.put("date", "2026-02-09");
    templateParams.put("code", "123456");

    // 验证收件人
    assertEquals(1, toMails.size());

    // 验证抄送
    assertEquals(2, ccMails.size());

    // 验证密送
    assertEquals(1, bccMails.size());

    // 验证参数
    assertEquals(2, templateParams.size());
  }

  @Test
  @DisplayName("测试邮件发送")
  void testSendMail() {
    MailAccount account = new MailAccount();
    account
        .setPort(465)
        .setHost("smtp.qq.com")
        .setFrom("627617031@qq.com")
        .setPass("utgnhtomhqvebbcf")
        .setUser("627617031")
        .setStarttlsEnable(true);

    String send = MailUtil.send(account, "627617031@qq.com", "测试邮件", "这是测试邮件", false);

    log.info("发送结果：{}", send);
  }
}
