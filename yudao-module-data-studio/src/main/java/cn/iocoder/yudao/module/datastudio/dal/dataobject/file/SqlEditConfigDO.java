package cn.iocoder.yudao.module.datastudio.dal.dataobject.file;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.file.handler.FlinkConfigTypeHandler;
import cn.iocoder.yudao.module.flink.common.dto.FlinkConfig;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * SQL编辑器配置表
 *
 * @author 芋道源码
 */
@TableName(value = "data_studio_sql_edit_config", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SqlEditConfigDO extends BaseDO {

    /**
     * 配置ID
     */
    private Long id;

    /**
     * SQL编辑器文件ID
     *
     * 关联 {@link SqlEditDO#id}
     */
    private Long sqlEditId;

    /**
     * Flink配置信息
     *
     * 使用JSON格式存储FlinkConfig对象
     */
    @TableField(typeHandler = FlinkConfigTypeHandler.class)
    private FlinkConfig config;



}
