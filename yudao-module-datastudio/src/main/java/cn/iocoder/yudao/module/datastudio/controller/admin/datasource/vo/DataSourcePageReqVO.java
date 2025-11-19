package cn.iocoder.yudao.module.datastudio.controller.admin.datasource.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据源配置分页 Request VO
 *
 * @author 芋道源码
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DataSourcePageReqVO extends PageParam {

    @Schema(description = "数据源名称", example = "MySQL生产库")
    private String name;

    @Schema(description = "数据源类型：mysql- MySQL，postgresql- PostgreSQL，oracle- Oracle，sqlserver- SQL Server，clickhouse- ClickHouse，hive- Hive", example = "mysql")
    private String type;

    @Schema(description = "状态：0-禁用，1-启用", example = "1")
    private Integer status;

}
