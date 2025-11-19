package cn.iocoder.yudao.module.datastudio.dal.dataobject.file;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.datastudio.dto.flink.FlinkConfig;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.lang.reflect.Field;

/**
 * SQL编辑器配置表
 *
 * @author 芋道源码
 */
@TableName(value = "data_studio_sql_edit_config", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SqlEditConfigDO extends BaseDO {

    /**
     * 配置ID
     */
    private Long id;

    /**
     * SQL编辑器文件ID
     *
     * 关联 {@link SqlEditDO#id}
     */
    private Long sqlEditId;

    /**
     * Flink配置信息
     *
     * 使用JSON格式存储FlinkConfig对象
     */
    @TableField(typeHandler = FlinkConfigTypeHandler.class)
    private FlinkConfig config;

    /**
     * Flink配置处理器
     * 用于将FlinkConfig对象与JSON字符串进行相互转换
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
            return JsonUtils.parseObject(json, FlinkConfig.class);
        }

        @Override
        public String toJson(FlinkConfig obj) {
            if (obj == null) {
                return null;
            }
            return JsonUtils.toJsonString(obj);
        }

    }

}
