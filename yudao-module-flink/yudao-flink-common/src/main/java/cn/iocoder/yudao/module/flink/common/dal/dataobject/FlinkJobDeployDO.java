package cn.iocoder.yudao.module.flink.common.dal.dataobject;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.datastudio.api.enums.JobStatus;
import cn.iocoder.yudao.module.flink.common.enums.JobTypeEnum;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;
import com.fasterxml.jackson.core.type.TypeReference;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * @author yzh
 */

/**
 * 任务执行记录表
 *
 * @author yzh
 */
@TableName(value = "data_studio_flink_job_deploy", autoResultMap = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlinkJobDeployDO {

  /** 执行ID */
  private Long id;

  /** 租户ID */
  private Long tenantId;

  /** 文件ID */
  private Long fileId;

  /** 集群ID */
  private Long clusterId;

  @Nullable
  /** Flink集群ID */
  private String flinkClusterId;

  /** 集群名称 */
  private String clusterName;

  /** 执行模式：stream/batch */
  private String executionMode;

  /** Flink版本 */
  private String flinkVersion;

  /** Flink作业ID */
  private String jobId;

  /** 作业名称 */
  private String jobName;

  /** 作业类型 */
  private JobTypeEnum jobType;

  /** 状态：pending-等待中，running-运行中，succeeded-成功，failed-失败，cancelled-已取消 */
  private @NonNull JobStatus status;

  /** 提交时间 */
  private LocalDateTime submitTime;

  /** 开始时间 */
  private LocalDateTime startTime;

  /** 结束时间 */
  private LocalDateTime endTime;

  /** 执行时长（毫秒） */
  private Long duration;

  /** 并行度 */
  private Integer parallelism;

  /** 检查点间隔（毫秒） */
  private Long checkpointInterval;

  /** 错误信息 */
  private String errorMessage;

  /** Flink Web UI链接 */
  private String webUiUrl;

  /** 配置信息 */
  @TableField(typeHandler = MapTypeHandler.class)
  private Map<String, String> config;

  /** 部署模式 */
  private String deployMode;

  public static class MapTypeHandler extends AbstractJsonTypeHandler<Map<String, String>> {

    public MapTypeHandler(Class<?> type) {
      super(type);
    }

    public MapTypeHandler(Class<?> type, Field field) {
      super(type, field);
    }

    @Override
    public Map<String, String> parse(String json) {
      return JsonUtils.parseObject(json, new TypeReference<Map<String, String>>() {});
    }

    @Override
    public String toJson(Map<String, String> obj) {
      return JsonUtils.toJsonString(obj);
    }
  }
}
