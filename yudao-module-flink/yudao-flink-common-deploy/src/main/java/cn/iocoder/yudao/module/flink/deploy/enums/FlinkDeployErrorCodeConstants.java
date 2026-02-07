package cn.iocoder.yudao.module.flink.deploy.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * Flink 部署模块错误码枚举
 *
 * <p>错误码范围：[1_003_000_000, 1_004_000_000)</p>
 */
public interface FlinkDeployErrorCodeConstants {

    // ========== 通用错误 1_003_000_xxx ==========
    ErrorCode UNSUPPORTED_DEPLOY_PARAM_TYPE = new ErrorCode(1_003_000_001,
            "不支持此类型执行参数{}，期望类型: {}");
    ErrorCode CLUSTER_RETRIEVE_FAILED = new ErrorCode(1_003_000_002,
            "获取集群失败: {}");
    ErrorCode JOB_SUBMIT_FAILED = new ErrorCode(1_003_000_003,
            "作业提交失败: {}");
    ErrorCode JOB_CANCEL_FAILED = new ErrorCode(1_003_000_004,
            "取消作业失败: {}");
    ErrorCode SQL_EXECUTION_FAILED = new ErrorCode(1_003_000_005,
            "执行 Flink SQL 作业失败: {}");

    // ========== YARN 部署错误 1_003_001_xxx ==========
    ErrorCode YARN_CLUSTER_DESCRIPTOR_FAILED = new ErrorCode(1_003_001_001,
            "获取 YARN 集群描述符失败: {}");
    ErrorCode YARN_APPLICATION_NOT_RUNNING = new ErrorCode(1_003_001_002,
            "YARN 应用未处于运行状态");
    ErrorCode YARN_JOB_DETAILS_FETCH_FAILED = new ErrorCode(1_003_001_003,
            "获取 YARN 作业详情失败: {}");
    ErrorCode YARN_DEPLOY_FAILED = new ErrorCode(1_003_001_004,
            "YARN 应用部署失败: {}");

    // ========== 数据摄取错误 1_003_002_xxx ==========
    ErrorCode DATA_INGESTION_PIPELINE_COMPOSER_FAILED = new ErrorCode(1_003_002_001,
            "创建 FlinkPipelineComposer 失败");
    ErrorCode DATA_INGESTION_LOCAL_DEPLOY_FAILED = new ErrorCode(1_003_002_002,
            "本地数据摄取部署失败: {}");
    ErrorCode DATA_INGESTION_REMOTE_DEPLOY_FAILED = new ErrorCode(1_003_002_003,
            "远程数据摄取部署失败: {}");
    ErrorCode DATA_INGESTION_YARN_DEPLOY_FAILED = new ErrorCode(1_003_002_004,
            "YARN 数据摄取部署失败: {}");
}
