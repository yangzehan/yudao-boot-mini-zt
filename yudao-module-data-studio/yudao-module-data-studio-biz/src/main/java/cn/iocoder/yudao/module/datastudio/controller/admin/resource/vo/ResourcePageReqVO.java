package cn.iocoder.yudao.module.datastudio.controller.admin.resource.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 数据中台 - 资源分页 Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "数据中台 - 资源分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class ResourcePageReqVO extends PageParam {

    @Schema(description = "资源名称", example = "JAR文件")
    private String name;

    @Schema(description = "资源描述", example = "Flink连接器")
    private String description;

}
