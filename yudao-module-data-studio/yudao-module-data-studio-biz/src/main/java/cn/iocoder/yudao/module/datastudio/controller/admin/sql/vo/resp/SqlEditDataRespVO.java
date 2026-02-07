package cn.iocoder.yudao.module.datastudio.controller.admin.sql.vo.resp;

import cn.iocoder.yudao.module.flink.common.dto.FlinkConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * SQL编辑器数据响应VO（包含文件信息和配置信息）
 *
 * @author 芋道源码
 */
@Data
@Schema(name = "SQL编辑器数据响应VO")
public class SqlEditDataRespVO {

  @Schema(name = "文件ID", example = "1")
  private Long id;

  @Schema(name = "文件/文件夹名称", example = "我的文件")
  private String name;

  @Schema(name = "文件类型：folder-文件夹，sql-sql文件，file-普通文件", example = "folder")
  private String type;

  @Schema(name = "父文件夹ID")
  private Long parentId;

  @Schema(name = "文件路径", example = "/project/sql/task1.sql")
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

  @Schema(name = "Flink配置信息", description = "SQL任务的执行配置")
  private FlinkConfig config;
}
