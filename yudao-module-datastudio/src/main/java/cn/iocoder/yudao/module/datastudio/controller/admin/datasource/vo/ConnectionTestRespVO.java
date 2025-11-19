package cn.iocoder.yudao.module.datastudio.controller.admin.datasource.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 连接测试结果 Response VO
 *
 * @author 芋道源码
 */
@Data
public class ConnectionTestRespVO {

    @Schema(description = "是否成功", example = "true")
    private Boolean success;

    @Schema(description = "响应时间（毫秒）", example = "1234")
    private Long responseTime;

    @Schema(description = "错误信息", example = "连接超时")
    private String errorMessage;

    @Schema(description = "连接信息", example = "连接成功，数据库版本：8.0.32")
    private String message;

}
