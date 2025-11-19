package cn.iocoder.yudao.module.datastudio.dal.dataobject.datasource;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据源配置表
 *
 * @author 芋道源码
 */
@TableName("data_studio_datasource")
@KeySequence("data_studio_datasource_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class DataSourceDO extends TenantBaseDO {

    /**
     * 数据源ID
     */
    @TableId
    private Long id;

    /**
     * 数据源名称
     */
    private String name;

    /**
     * 数据源类型：mysql- MySQL，postgresql- PostgreSQL，oracle- Oracle，sqlserver- SQL Server，clickhouse- ClickHouse，hive- Hive
     */
    private String type;

    /**
     * 数据库驱动类名
     */
    @TableField("`driver_class_name`")
    private String driverClassName;

    /**
     * 连接URL
     */
    private String url;

    /**
     * 主机地址
     */
    private String host;

    /**
     * 端口号
     */
    private Integer port;

    /**
     * 数据库名称
     */
    @TableField("`database`")
    private String database;

    /**
     * 用户名
     */
    @TableField("`username`")
    private String username;

    /**
     * 密码（加密存储）
     */
    @TableField("`password`")
    private String password;

    /**
     * 连接参数（JSON格式）
     */
    @TableField("`connection_params`")
    private String connectionParams;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 连接状态：connecting-连接中，connected-已连接，disconnected-未连接，error-连接错误
     */
    @TableField("`connection_status`")
    private String connectionStatus;

    /**
     * 连接测试时间
     */
    @TableField("`last_connection_time`")
    private Long lastConnectionTime;

    /**
     * 连接错误信息
     */
    @TableField("`last_connection_error`")
    private String lastConnectionError;

    /**
     * 显示顺序
     */
    private Integer sort;

    /**
     * 描述信息
     */
    private String description;

    /**
     * 扩展信息（JSON格式）
     */
    @TableField("`ext_info`")
    private String extInfo;

}