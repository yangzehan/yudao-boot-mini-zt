package cn.iocoder.yudao.module.datastudio.controller.admin.datasource.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 数据源类型 Response VO
 *
 * @author 芋道源码
 */
@Data
public class DataSourceTypeRespVO {

    @Schema(description = "类型标识", example = "mysql")
    private String type;

    @Schema(description = "类型名称", example = "MySQL")
    private String name;

    @Schema(description = "驱动类名", example = "com.mysql.cj.jdbc.Driver")
    private String driverClassName;

    @Schema(description = "默认端口", example = "3306")
    private Integer defaultPort;

    @Schema(description = "默认URL模板", example = "jdbc:mysql://{host}:{port}/{database}")
    private String defaultUrlTemplate;

    @Schema(description = "默认驱动包", example = "mysql:mysql-connector-java")
    private String defaultDriver;

    @Schema(description = "是否支持", example = "true")
    private Boolean supported;

    @Schema(description = "图标", example = "mysql")
    private String icon;

}
