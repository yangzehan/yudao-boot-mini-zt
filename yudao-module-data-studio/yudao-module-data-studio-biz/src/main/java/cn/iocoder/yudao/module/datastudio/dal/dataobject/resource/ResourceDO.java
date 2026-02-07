package cn.iocoder.yudao.module.datastudio.dal.dataobject.resource;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据中台 - 资源 DO
 *
 * @author 芋道源码
 */
@TableName("data_studio_resource")
@KeySequence("data_studio_resource_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class ResourceDO extends TenantBaseDO {

    /**
     * 资源ID
     */
    @TableId
    private Long id;

    /**
     * 资源名称
     */
    private String name;

    /**
     * 资源路径
     */
    private String path;

    /**
     * 文件大小（字节）
     */
    private Long size;

    /**
     * 资源描述
     */
    private String description;

    /**
     * 文件访问URL
     */
    private String fileUrl;

}
