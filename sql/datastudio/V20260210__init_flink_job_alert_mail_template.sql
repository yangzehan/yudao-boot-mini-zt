-- Flink 作业告警邮件模板初始化脚本
-- 执行前请确保已创建 system_mail_template 表
-- 如果使用数据工作室模块，请使用对应的表前缀

-- 插入 Flink 作业失败告警模板
INSERT INTO system_mail_template (
    id,
    name,
    code,
    account_id,
    nickname,
    title,
    content,
    params,
    status,
    remark,
    creator,
    create_time,
    updater,
    update_time,
    deleted
) VALUES (
    1001,
    'Flink作业失败告警',
    'flink_job_failed_alert',
    1,  -- 请替换为实际的邮箱账号ID
    'Flink监控系统',
    '【严重告警】Flink作业 ${jobName} 运行失败',
    '<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <style>
        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
        .header { background: linear-gradient(135deg, #e74c3c, #c0392b); color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }
        .content { background: #f9f9f9; padding: 20px; border: 1px solid #ddd; }
        .info-table { width: 100%; border-collapse: collapse; margin: 15px 0; }
        .info-table th, .info-table td { padding: 10px; border-bottom: 1px solid #ddd; text-align: left; }
        .info-table th { background: #f5f5f5; width: 120px; }
        .footer { background: #f5f5f5; padding: 15px; text-align: center; font-size: 12px; color: #999; border-radius: 0 0 5px 5px; }
        .warning { color: #e74c3c; font-weight: bold; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>⚠️ Flink 作业失败告警</h1>
        </div>
        <div class="content">
            <p>您好，系统检测到 Flink 作业发生故障，请及时处理。</p>

            <table class="info-table">
                <tr>
                    <th>作业名称</th>
                    <td><strong>${jobName}</strong></td>
                </tr>
                <tr>
                    <th>作业ID</th>
                    <td>${jobId}</td>
                </tr>
                <tr>
                    <th>发生时间</th>
                    <td>${occurredTime}</td>
                </tr>
                <tr>
                    <th>错误信息</th>
                    <td class="warning">${errorMsg}</td>
                </tr>
            </table>

            <p><strong>建议操作：</strong></p>
            <ol>
                <li>登录 Flink Web UI 检查作业日志</li>
                <li>查看详细的异常堆栈信息</li>
                <li>根据错误信息修复作业问题</li>
                <li>重启作业并验证运行状态</li>
            </ol>

            <p style="text-align: center; margin-top: 20px;">
                <a href="#" style="background: #3498db; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">查看作业详情</a>
            </p>
        </div>
        <div class="footer">
            <p>此邮件由 Flink 监控系统自动发送，请勿回复。</p>
            <p>如有问题，请联系系统管理员。</p>
        </div>
    </div>
</body>
</html>',
    '["jobName", "jobId", "errorMsg", "occurredTime"]',
    0,  -- 0: 启用, 1: 禁用
    'Flink作业失败时自动发送的告警邮件',
    'system',
    NOW(),
    'system',
    NOW(),
    0
);

-- 插入 Flink 作业恢复通知模板
INSERT INTO system_mail_template (
    id,
    name,
    code,
    account_id,
    nickname,
    title,
    content,
    params,
    status,
    remark,
    creator,
    create_time,
    updater,
    update_time,
    deleted
) VALUES (
    1002,
    'Flink作业恢复通知',
    'flink_job_recovery_alert',
    1,  -- 请替换为实际的邮箱账号ID
    'Flink监控系统',
    '✅ Flink作业 ${jobName} 已恢复正常运行',
    '<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <style>
        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
        .header { background: linear-gradient(135deg, #27ae60, #2ecc71); color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }
        .content { background: #f9f9f9; padding: 20px; border: 1px solid #ddd; }
        .info-table { width: 100%; border-collapse: collapse; margin: 15px 0; }
        .info-table th, .info-table td { padding: 10px; border-bottom: 1px solid #ddd; text-align: left; }
        .info-table th { background: #f5f5f5; width: 120px; }
        .footer { background: #f5f5f5; padding: 15px; text-align: center; font-size: 12px; color: #999; border-radius: 0 0 5px 5px; }
        .success { color: #27ae60; font-weight: bold; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>✅ Flink 作业恢复通知</h1>
        </div>
        <div class="content">
            <p>您好，之前发生故障的 Flink 作业已恢复正常运行。</p>

            <table class="info-table">
                <tr>
                    <th>作业名称</th>
                    <td><strong>${jobName}</strong></td>
                </tr>
                <tr>
                    <th>作业ID</th>
                    <td>${jobId}</td>
                </tr>
                <tr>
                    <th>恢复时间</th>
                    <td class="success">${recoveredTime}</td>
                </tr>
                <tr>
                    <th>告警触发次数</th>
                    <td>${alertCount} 次</td>
                </tr>
            </table>

            <p style="background: #e8f5e9; padding: 10px; border-left: 4px solid #27ae60; margin: 15px 0;">
                作业已恢复正常运行，建议继续观察一段时间以确保稳定。
            </p>
        </div>
        <div class="footer">
            <p>此邮件由 Flink 监控系统自动发送，请勿回复。</p>
            <p>如有问题，请联系系统管理员。</p>
        </div>
    </div>
</body>
</html>',
    '["jobName", "jobId", "recoveredTime", "alertCount"]',
    0,  -- 0: 启用, 1: 禁用
    'Flink作业从失败状态恢复时发送的通知邮件',
    'system',
    NOW(),
    'system',
    NOW(),
    0
);

-- 注意：
-- 1. 请将 account_id 替换为实际存在的邮箱账号ID
-- 2. 可以通过以下SQL查询已有的邮箱账号：
--    SELECT id, mail FROM system_mail_account WHERE deleted = 0;
-- 3. 模板参数说明：
--    - jobName: 作业名称
--    - jobId: 作业ID (Flink Job ID)
--    - errorMsg: 错误信息
--    - occurredTime: 告警发生时间
--    - recoveredTime: 作业恢复时间
--    - alertCount: 告警触发次数
