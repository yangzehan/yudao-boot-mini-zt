package cn.iocoder.yudao.module.datastudio.controller.admin.datasource.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据源配置 Response VO
 *
 * @author 芋道源码
 */
@Data
public class DataSourceRespVO {

    @Schema(description = "数据源ID", example = "1024")
    private Long id;

    @Schema(description = "数据源名称", example = "MySQL生产库")
    private String name;

    @Schema(description = "数据源类型：mysql- MySQL，postgresql- PostgreSQL，oracle- Oracle，sqlserver- SQL Server，clickhouse- ClickHouse，hive- Hive", example = "mysql")
    private String type;

    @Schema(description = "数据库驱动类名", example = "com.mysql.cj.jdbc.Driver")
    private String driverClassName;

    @Schema(description = "连接URL", example = "jdbc:mysql://localhost:3306/test")
    private String url;

    @Schema(description = "主机地址", example = "localhost")
    private String host;

    @Schema(description = "端口号", example = "3306")
    private Integer port;

    @Schema(description = "数据库名称", example = "test_db")
    private String database;

    @Schema(description = "状态：0-禁用，1-启用", example = "1")
    private Integer status;

    @Schema(description = "连接状态：connecting-连接中，connected-已连接，disconnected-未连接，error-连接错误", example = "connected")
    private String connectionStatus;

    @Schema(description = "连接测试时间")
    private LocalDateTime lastConnectionTime;

    @Schema(description = "连接错误信息")
    private String lastConnectionError;

    @Schema(description = "显示顺序", example = "1024")
    private Integer sort;

    @Schema(description = "描述信息", example = "这是我的数据源")
    private String description;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
