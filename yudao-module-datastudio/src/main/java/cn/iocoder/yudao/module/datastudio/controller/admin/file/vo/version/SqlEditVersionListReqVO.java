package cn.iocoder.yudao.module.datastudio.controller.admin.file.vo.version;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 * SQL编辑器版本列表请求 VO
 *
 * @author 芋道源码
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(name = "SQL编辑器版本列表请求 VO")
public class SqlEditVersionListReqVO extends PageParam {

    @Schema(name = "文件ID", required = true, example = "1")
    @NotNull(message = "文件ID不能为空")
    private Long sqlEditId;

}
