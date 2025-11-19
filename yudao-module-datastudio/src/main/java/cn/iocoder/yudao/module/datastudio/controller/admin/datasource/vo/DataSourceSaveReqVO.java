package cn.iocoder.yudao.module.datastudio.controller.admin.datasource.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 数据源配置保存 Request VO
 *
 * @author 芋道源码
 */
@Data
public class DataSourceSaveReqVO {

    @Schema(description = "数据源ID", example = "1024")
    private Long id;

    @Schema(description = "数据源名称", required = true, example = "MySQL生产库")
    @NotBlank(message = "数据源名称不能为空")
    private String name;

    @Schema(description = "数据源类型：mysql- MySQL，postgresql- PostgreSQL，oracle- Oracle，sqlserver- SQL Server，clickhouse- ClickHouse，hive- Hive", required = true, example = "mysql")
    @NotBlank(message = "数据源类型不能为空")
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

    @Schema(description = "用户名", example = "root")
    private String username;

    @Schema(description = "密码", example = "123456")
    private String password;

    @Schema(description = "连接参数（JSON格式）")
    private String connectionParams;

    @Schema(description = "状态：0-禁用，1-启用", example = "1")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "显示顺序", example = "1024")
    private Integer sort;

    @Schema(description = "描述信息", example = "这是我的数据源")
    private String description;

    @Schema(description = "扩展信息（JSON格式）")
    private String extInfo;

}
