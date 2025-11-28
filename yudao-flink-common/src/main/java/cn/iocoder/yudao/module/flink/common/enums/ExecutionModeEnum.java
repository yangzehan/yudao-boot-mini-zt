package cn.iocoder.yudao.module.flink.common.enums;

import cn.iocoder.yudao.module.flink.common.executor.FlinkJobExecutor;
import cn.iocoder.yudao.module.flink.common.executor.FlinkJobReMoTeExecutor;
import lombok.Getter;


@Getter
public enum ExecutionModeEnum {


    REMOTE("remote", FlinkJobReMoTeExecutor.class);

    private final Class<? extends FlinkJobExecutor> flinkJobExecutorClass;
    private final String name;

    ExecutionModeEnum(String name, Class<? extends FlinkJobExecutor> flinkJobExecutorClass) {
        this.name = name;
        this.flinkJobExecutorClass = flinkJobExecutorClass;
    }

}
