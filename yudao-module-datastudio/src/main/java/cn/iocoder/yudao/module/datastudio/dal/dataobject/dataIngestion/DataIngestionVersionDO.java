package cn.iocoder.yudao.module.datastudio.dal.dataobject.dataIngestion;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.lang.reflect.Field;

/**
 * 数据摄取版本 DO
 *
 * @author 芋道源码
 */
@TableName(value = "data_studio_data_ingestion_version", autoResultMap = true)
@KeySequence("data_studio_data_ingestion_version_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class DataIngestionVersionDO extends TenantBaseDO {

    /**
     * 版本ID
     */
    @TableId
    private Long id;

    /**
     * 文件ID
     */
    private Long dataIngestionId;

    /**
     * 版本号（通过应用层自增）
     */
    private Long versionNumber;

    /**
     * 文件内容快照
     */
    private String content;

    /**
     * 配置信息快照（JSON格式）
     */
    @TableField(typeHandler = ConfigTypeHandler.class)
    private ConfigInfo config;

    /**
     * 版本备注
     */
    private String remark;

    /**
     * 版本类型：manual-手动保存, auto-自动保存
     */
    private String versionType;

    /**
     * 配置信息实体类
     */
    @Data
    public static class ConfigInfo {
        /**
         * 文件类型：folder-文件夹，yaml-yaml文件，file-普通文件
         */
        private String type;

        /**
         * 父文件夹ID
         */
        private Long parentId;

        /**
         * 文件路径
         */
        private String filePath;

        /**
         * 显示顺序
         */
        private Integer sort;

        /**
         * 文件大小（字节）
         */
        private Long fileSize;

        /**
         * 状态：0-禁用，1-启用
         */
        private Integer status;

        // Flink任务配置字段
        /**
         * 执行模式：local-本地模式，remote-远程模式，cluster-集群模式
         */
        private String executionMode;

        /**
         * Flink版本
         */
        private String flinkVersion;

        /**
         * 并行度
         */
        private Integer parallelism;

        /**
         * 检查点间隔（毫秒）
         */
        private Long checkpointInterval;
    }

    /**
     * 配置信息类型处理器
     */
    public static class ConfigTypeHandler extends AbstractJsonTypeHandler<ConfigInfo> {

        public ConfigTypeHandler(Class<?> type) {
            super(type);
        }

        public ConfigTypeHandler(Class<?> type, Field field) {
            super(type, field);
        }

        @Override
        public ConfigInfo parse(String json) {
            if (json == null || json.trim().isEmpty()) {
                return null;
            }
            return cn.iocoder.yudao.framework.common.util.json.JsonUtils.parseObject(json, ConfigInfo.class);
        }

        @Override
        public String toJson(ConfigInfo obj) {
            if (obj == null) {
                return null;
            }
            return cn.iocoder.yudao.framework.common.util.json.JsonUtils.toJsonString(obj);
        }
    }
}
