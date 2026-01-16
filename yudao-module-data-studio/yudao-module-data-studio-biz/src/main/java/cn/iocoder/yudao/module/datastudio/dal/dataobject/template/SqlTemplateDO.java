package cn.iocoder.yudao.module.datastudio.dal.dataobject.template;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * SQL 模板表
 *
 * @author admin
 */
@TableName("data_studio_sql_template")
@KeySequence("data_studio_sql_template_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class SqlTemplateDO extends TenantBaseDO {

    @TableId
    private Long id;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 模板描述
     */
    private String description;

    /**
     * 模板分类：datastream-datastream作业，datasource-数据源，sql-通用SQL，etl-数据清洗，alert-告警
     */
    private String category;

    /**
     * SQL模板内容（支持占位符 ${table} 等）
     */
    private String content;

    /**
     * 默认Flink配置（JSON格式）
     */
    private String defaultConfig;

    /**
     * 显示顺序
     */
    private Integer sort;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 占位符配置（JSON格式，存储每个占位符的标签和提示信息）
     * 格式: {"mysql_host": {"label": "MySQL 主机", "hint": "例如: localhost 或 127.0.0.1"}, ...}
     */
    private String placeholderConfig;

}
