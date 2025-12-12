package cn.iocoder.yudao.module.datastudio.dal.dataobject.dataIngestion;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.file.handler.FlinkConfigTypeHandler;
import cn.iocoder.yudao.module.flink.common.dto.FlinkConfig;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据摄取配置 DO
 *
 * @author 芋道源码
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "data_studio_data_ingestion_config", autoResultMap = true)
@KeySequence("data_studio_data_ingestion_config_seq")
public class DataIngestionConfigDO extends TenantBaseDO {

    /**
     * ID
     */
    private Long id;
    
    /**
     * 数据摄取文件ID
     */
    private Long dataIngestionId;
    
    /**
     * 配置信息
     */
    @TableField(typeHandler = FlinkConfigTypeHandler.class)
    private FlinkConfig config;

}
