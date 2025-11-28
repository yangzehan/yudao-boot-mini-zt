package cn.iocoder.yudao.module.datastudio.framework.flink.client;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.util.spring.SpringUtils;
import cn.iocoder.yudao.module.flink.common.api.Flink118Api;
import cn.iocoder.yudao.module.flink.common.api.FlinkApi;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;



/**
 * FlinkApi工厂类，用于根据Flink版本获取对应的FlinkApi实例
 * @author yzh
 */

public class FlinkApiFactory {
    private static final Map<String,Class<? extends FlinkApi>> FLINK_API_MAP =new ConcurrentHashMap<>();


    static {
        FLINK_API_MAP.put("1.18", Flink118Api.class);
    }


        /**
     * 根据Flink版本获取对应的FlinkApi实例
     *
     * @param version Flink版本号
     * @return 对应版本的FlinkApi实例
     * @throws ServiceException 当找不到对应版本的FlinkApi时抛出异常
     */
    public static FlinkApi getFlinkApiByVersion(String version){
        Class<? extends FlinkApi> flinkApiClass = FLINK_API_MAP.get(version);
        if (flinkApiClass ==null) {
            throw ServiceExceptionUtil.exception(new ErrorCode(9999, "请选择正确的Flink版本"));
        }
        return SpringUtils.getBean(flinkApiClass);

    }



}
