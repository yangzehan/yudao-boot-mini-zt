package cn.iocoder.yudao.module.datastudio.dal.dataobject.file.handler;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.datastudio.dto.flink.FlinkConfig;
import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;

import java.lang.reflect.Field;

/**
 * Flink配置处理器
 * 用于将FlinkConfig对象与JSON字符串进行相互转换
 */
public  class FlinkConfigTypeHandler extends AbstractJsonTypeHandler<FlinkConfig> {


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