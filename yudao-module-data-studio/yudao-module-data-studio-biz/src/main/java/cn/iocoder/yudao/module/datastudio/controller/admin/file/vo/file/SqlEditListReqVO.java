package cn.iocoder.yudao.module.datastudio.controller.admin.file.vo.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Size;

/**
 * SQL 编辑器列表查询请求VO
 *
 * @author 芋道源码
 */
@Data
@Schema(name = "SQL 编辑器列表查询请求VO")
public class SqlEditListReqVO {

    @Schema(name = "搜索关键词")
    @Size(max = 100, message = "关键词长度不能超过100个字符")
    private String keyword;

    @Schema(name = "文件类型：folder-文件夹，sql-sql文件，file-普通文件")
    @Size(max = 20, message = "文件类型长度不能超过20个字符")
    private String type;

    @Schema(name = "父ID")
    private Long parentId;

}
