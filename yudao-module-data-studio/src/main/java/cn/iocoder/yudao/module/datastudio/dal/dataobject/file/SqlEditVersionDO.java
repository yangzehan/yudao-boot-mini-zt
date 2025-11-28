package cn.iocoder.yudao.module.datastudio.dal.dataobject.file;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.file.handler.FlinkConfigTypeHandler;
import cn.iocoder.yudao.module.flink.common.dto.FlinkConfig;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * SQL编辑器版本 DO
 *
 * @author 芋道源码
 */
@TableName(value = "data_studio_sql_edit_version", autoResultMap = true)
@KeySequence("data_studio_sql_edit_version_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class SqlEditVersionDO extends TenantBaseDO {

    /**
     * 版本ID
     */
    @TableId
    private Long id;
    /**
     * 文件ID
     */
    private Long sqlEditId;
    /**
     * 版本号（通过应用层自增）
     */
    private Long versionNumber;
    /**
     * 脚本内容快照
     */
    private String content;
    /**
     * Flink配置快照（JSON格式）
     */
    @TableField(typeHandler = FlinkConfigTypeHandler.class)
    private FlinkConfig config;
    /**
     * 版本备注
     */
    private String remark;
    /**
     * 版本类型：manual-手动保存, auto-自动保存
     */
    private String versionType;

}
