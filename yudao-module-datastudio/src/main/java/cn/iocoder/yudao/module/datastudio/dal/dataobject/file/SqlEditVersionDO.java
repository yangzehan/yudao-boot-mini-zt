package cn.iocoder.yudao.module.datastudio.dal.dataobject.file;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import cn.iocoder.yudao.module.datastudio.dto.flink.FlinkConfig;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.lang.reflect.Field;

/**
 * SQL编辑器版本 DO
 *
 * @author 芋道源码
 */
@TableName(value = "data_studio_sql_edit_version", autoResultMap = true)
@KeySequence("data_studio_sql_edit_version_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class SqlEditVersionDO extends TenantBaseDO {

    /**
     * 版本ID
     */
    @TableId
    private Long id;
    /**
     * 文件ID
     */
    private Long sqlEditId;
    /**
     * 版本号（通过应用层自增）
     */
    private Long versionNumber;
    /**
     * 脚本内容快照
     */
    private String content;
    /**
     * Flink配置快照（JSON格式）
     */
    @TableField(typeHandler = FlinkConfigTypeHandler.class)
    private FlinkConfig config;
    /**
     * 版本备注
     */
    private String remark;
    /**
     * 版本类型：manual-手动保存, auto-自动保存
     */
    private String versionType;

    /**
     * Flink配置类型处理器
     */
    public static class FlinkConfigTypeHandler extends AbstractJsonTypeHandler<FlinkConfig> {

        public FlinkConfigTypeHandler(Class<?> type) {
            super(type);
        }

        public FlinkConfigTypeHandler(Class<?> type, Field field) {
            super(type, field);
        }

        @Override
        public FlinkConfig parse(String json) {
            if (json == null || json.trim().isEmpty()) {
                return null;
            }
            return cn.iocoder.yudao.framework.common.util.json.JsonUtils.parseObject(json, FlinkConfig.class);
        }

        @Override
        public String toJson(FlinkConfig obj) {
            if (obj == null) {
                return null;
            }
            return cn.iocoder.yudao.framework.common.util.json.JsonUtils.toJsonString(obj);
        }
    }
}
