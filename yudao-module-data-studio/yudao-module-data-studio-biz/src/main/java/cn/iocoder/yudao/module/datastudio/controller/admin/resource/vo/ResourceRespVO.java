package cn.iocoder.yudao.module.datastudio.controller.admin.resource.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据中台 - 资源 Response VO
 *
 * @author 芋道源码
 */
@Schema(description = "数据中台 - 资源 Response VO")
@Data
public class ResourceRespVO {

    @Schema(description = "资源ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "资源名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "flink-connector.jar")
    private String name;

    @Schema(description = "资源路径", example = "/data-studio/resource/flink-connector.jar")
    private String path;

    @Schema(description = "文件大小（字节）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1048576")
    private Long size;

    @Schema(description = "资源描述", example = "Flink MySQL 连接器")
    private String description;

    @Schema(description = "文件访问URL", example = "http://xxx/xxx/xxx.jar")
    private String fileUrl;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
