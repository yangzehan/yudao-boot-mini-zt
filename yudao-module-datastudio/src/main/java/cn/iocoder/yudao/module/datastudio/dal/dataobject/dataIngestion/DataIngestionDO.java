package cn.iocoder.yudao.module.datastudio.dal.dataobject.dataIngestion;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据摄取文件表
 *
 * @author 芋道源码
 */
@TableName("data_studio_data_ingestion")
@KeySequence("data_studio_data_ingestion_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class DataIngestionDO extends TenantBaseDO {

    /**
     * 文件ID
     */
    @TableId
    private Long id;

    /**
     * 文件/文件夹名称
     */
    private String name;

    /**
     * 文件类型：folder-文件夹，yaml-yaml文件，file-普通文件
     */
    private String type;

    /**
     * 父文件夹ID
     *
     * 关联 {@link #id}
     */
    private Long parentId;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件内容（仅对文件有效，文件夹为空）
     */
    private String content;

    /**
     * 显示顺序
     */
    private Integer sort;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

}
