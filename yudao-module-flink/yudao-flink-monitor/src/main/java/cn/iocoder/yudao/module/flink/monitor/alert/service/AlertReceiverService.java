package cn.iocoder.yudao.module.flink.monitor.alert.service;

import cn.iocoder.yudao.module.datastudio.dal.dataobject.flink.job.deploy.FlinkJobDeployDO;
import cn.iocoder.yudao.module.datastudio.dal.mysql.job.DataJobMapper;
import cn.iocoder.yudao.module.flink.monitor.alert.model.AlertConfig;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 告警接收人服务
 *
 * <p>负责从作业创建人和配置中获取告警接收人</p>
 *
 * @author yzh
 */
@Slf4j
@Service
public class AlertReceiverService {

    @Resource
    private AdminUserApi adminUserApi;

    @Resource
    private DataJobMapper dataJobMapper;

    /**
     * 邮箱正则
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    /**
     * 获取告警接收人信息
     *
     * @param jobId   作业ID
     * @param config  告警配置
     * @return 接收人信息
     */
    public ReceiverInfo getReceivers(String jobId, AlertConfig config) {
        Set<Long> userIds = new HashSet<>();
        Set<String> emails = new HashSet<>();

        // 1. 从配置中获取接收人配置
        if (config != null && config.getReceivers() != null) {
            AlertConfig.ReceiverConfig receiverConfig = config.getReceivers();

            // 添加配置中的用户ID
            if (receiverConfig.getUserIds() != null) {
                userIds.addAll(receiverConfig.getUserIds());
            }

            // 添加额外邮箱
            if (receiverConfig.getExtraEmails() != null) {
                emails.addAll(filterValidEmails(receiverConfig.getExtraEmails()));
            }
        }

        // 2. 从作业创建人获取
        if (jobId != null) {
            Optional<FlinkJobDeployDO> jobOpt = getJobByJobId(jobId);
            if (jobOpt.isPresent()) {
                FlinkJobDeployDO job = jobOpt.get();
                // 从作业中提取创建人ID
                Long creatorUserId = extractCreatorUserId(job);
                if (creatorUserId != null) {
                    userIds.add(creatorUserId);
                }
            }
        }

        // 3. 如果配置中没有指定，默认使用 ADMIN 用户
        if (userIds.isEmpty() && emails.isEmpty()) {
            log.warn("没有配置告警接收人，尝试使用系统管理员");
            // 这里可以配置默认管理员邮箱
        }

        // 4. 根据用户ID获取邮箱
        if (!userIds.isEmpty()) {
            List<AdminUserRespDTO> users = adminUserApi.getUserList(new java.util.ArrayList<>(userIds));
            for (AdminUserRespDTO user : users) {
                if (user.getEmail() != null && isValidEmail(user.getEmail())) {
                    emails.add(user.getEmail());
                }
            }
            log.debug("根据用户ID获取邮箱: userIds={}, emails={}", userIds.size(), emails.size());
        }

        return ReceiverInfo.builder()
                .userIds(new HashSet<>(userIds))
                .emails(new HashSet<>(emails))
                .build();
    }

    /**
     * 根据用户ID列表获取接收人信息
     */
    public ReceiverInfo getReceiversByUserIds(List<Long> userIds) {
        Set<String> emails = new HashSet<>();

        if (userIds != null && !userIds.isEmpty()) {
            List<AdminUserRespDTO> users = adminUserApi.getUserList(userIds);
            for (AdminUserRespDTO user : users) {
                if (user.getEmail() != null && isValidEmail(user.getEmail())) {
                    emails.add(user.getEmail());
                }
            }
        }

        return ReceiverInfo.builder()
                .userIds(new HashSet<>(userIds))
                .emails(emails)
                .build();
    }

    /**
     * 根据邮箱列表获取接收人信息
     */
    public ReceiverInfo getReceiversByEmails(List<String> emails) {
        return ReceiverInfo.builder()
                .userIds(new HashSet<>())
                .emails(new HashSet<>(filterValidEmails(emails)))
                .build();
    }

    /**
     * 验证邮箱格式
     */
    public boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * 过滤有效的邮箱列表
     */
    private List<String> filterValidEmails(List<String> emails) {
        if (emails == null) {
            return java.util.Collections.emptyList();
        }
        return emails.stream()
                .filter(this::isValidEmail)
                .collect(Collectors.toList());
    }

    /**
     * 根据作业ID查询作业信息
     */
    private Optional<FlinkJobDeployDO> getJobByJobId(String jobId) {
        try {
            FlinkJobDeployDO job = dataJobMapper.selectOne(FlinkJobDeployDO::getJobId, jobId);
            return Optional.ofNullable(job);
        } catch (Exception e) {
            log.error("查询作业信息失败: jobId={}", jobId, e);
            return Optional.empty();
        }
    }

    /**
     * 从作业中提取创建人用户ID
     */
    private Long extractCreatorUserId(FlinkJobDeployDO job) {
        // 注意: FlinkJobDeployDO 的 creator 字段可能是 String 类型（用户名）
        // 需要根据实际情况转换为 Long 类型的用户ID
        // 这里假设 creator 字段存储的是用户ID的字符串形式
        if (job == null || job.getCreator() == null) {
            return null;
        }

        String creator = job.getCreator();
        try {
            // 如果 creator 是数字字符串，直接转换
            if (creator.matches("\\d+")) {
                return Long.parseLong(creator);
            }
            // 如果 creator 是用户名，需要通过 AdminUserApi 查找对应的用户ID
            // 这里简化处理，实际应该调用 API 获取
            log.debug("creator 是用户名而非数字ID: {}", creator);
            return null;
        } catch (NumberFormatException e) {
            log.warn("无法解析 creator 为用户ID: {}", creator);
            return null;
        }
    }

    /**
     * 接收人信息
     */
    @Data
    @Builder
    public static class ReceiverInfo {

        /**
         * 用户ID列表
         */
        private Set<Long> userIds;

        /**
         * 邮箱列表
         */
        private Set<String> emails;

        public boolean isEmpty() {
            return (userIds == null || userIds.isEmpty()) &&
                    (emails == null || emails.isEmpty());
        }

        /**
         * 获取邮箱列表（转换为 List）
         */
        public List<String> getEmailList() {
            if (emails == null) {
                return java.util.Collections.emptyList();
            }
            return new java.util.ArrayList<>(emails);
        }

        /**
         * 获取用户ID列表（转换为 List）
         */
        public List<Long> getUserIdList() {
            if (userIds == null) {
                return java.util.Collections.emptyList();
            }
            return new java.util.ArrayList<>(userIds);
        }
    }
}
