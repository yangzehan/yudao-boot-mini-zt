package cn.iocoder.yudao.module.datastudio.controller.admin.dataIngestion.vo.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据摄取响应VO
 *
 * @author 芋道源码
 */
@Data
@Schema(name = "数据摄取响应VO")
public class DataIngestionRespVO {

    @Schema(name = "文件ID", example = "1")
    private Long id;

    @Schema(name = "文件/文件夹名称", example = "我的文件")
    private String name;

    @Schema(name = "文件类型：folder-文件夹，yaml-yaml文件，file-普通文件", example = "folder")
    private String type;

    @Schema(name = "父文件夹ID")
    private Long parentId;

    @Schema(name = "文件路径", example = "/datastudio/ingestion/config.yaml")
    private String filePath;

    @Schema(name = "文件内容")
    private String content;

    @Schema(name = "显示顺序", example = "0")
    private Integer sort;

    @Schema(name = "文件大小（字节）", example = "1024")
    private Long fileSize;

    @Schema(name = "状态：0-禁用，1-启用", example = "1")
    private Integer status;

    @Schema(name = "创建时间")
    private LocalDateTime createTime;

    @Schema(name = "更新时间")
    private LocalDateTime updateTime;

    @Schema(name = "子文件列表（用于树形结构）")
    private List<DataIngestionRespVO> children;

}
