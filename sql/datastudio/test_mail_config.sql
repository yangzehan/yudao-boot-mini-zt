-- =====================================================
-- 邮件测试配置 SQL
-- 用于配置 QQ 邮箱 SMTP 和测试模板
-- 执行前请确保已导入基础表结构
-- =====================================================

-- 配置邮件账号 (QQ邮箱 SMTP)
INSERT INTO system_mail_account (id, mail, username, password, host, port, ssl_enable, starttls_enable, creator,
                                 create_time, updater, update_time, deleted)
VALUES (100, '627617031@qq.com', '627617031@qq.com', 'utgnhtomhqvebbcf', 'smtp.qq.com', 465, TRUE, FALSE, 'admin',
        NOW(), 'admin', NOW(), FALSE);

-- 配置测试邮件模板 (test_01)
INSERT INTO system_mail_template (id, name, code, account_id, nickname, title, content, params, status, remark, creator,
                                  create_time, updater, update_time, deleted)
VALUES (100, '测试邮件模板', 'test_01', 100, '芋道源码', '【测试】这是一封测试邮件',
        '<p>你好，这是一封测试邮件！</p>
        <p>测试时间: ${date}</p>
        <p>如果你收到这封邮件，说明邮件配置成功。</p>
        <p>-- 芋道源码团队</p>',
        '["date"]', 0, '用于测试邮件发送功能', 'admin', NOW(), 'admin', NOW(), FALSE);
