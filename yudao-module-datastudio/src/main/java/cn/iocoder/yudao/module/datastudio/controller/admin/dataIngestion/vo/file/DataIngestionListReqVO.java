package cn.iocoder.yudao.module.datastudio.controller.admin.dataIngestion.vo.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据摄取列表请求VO
 *
 * @author 芋道源码
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(name = "数据摄取列表请求VO")
public class DataIngestionListReqVO {

    @Schema(name = "搜索关键词", example = "test")
    private String keyword;

    @Schema(name = "文件类型", example = "folder")
    private String type;

    @Schema(name = "父文件夹ID", example = "1")
    private Long parentId;

}
