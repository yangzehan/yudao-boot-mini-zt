package cn.iocoder.yudao.module.flink.common.factory;

import cn.iocoder.yudao.module.flink.common.enums.ExecutionModeEnum;
import cn.iocoder.yudao.module.flink.common.executor.FlinkJobExecutor;

public interface FlinkJobExecuteFactory {
    static FlinkJobExecutor getExecutorFactoryByExecutionMode(String executionMode) {
        ExecutionModeEnum.valueOf(executionMode);




        return null;
    }
}
