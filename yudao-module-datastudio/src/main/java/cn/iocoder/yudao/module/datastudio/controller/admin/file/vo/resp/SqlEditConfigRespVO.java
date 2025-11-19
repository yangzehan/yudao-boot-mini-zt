package cn.iocoder.yudao.module.datastudio.controller.admin.file.vo.resp;

import cn.iocoder.yudao.module.datastudio.dto.flink.FlinkConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * SQL编辑器配置响应VO
 *
 * @author 芋道源码
 */
@Data
@Schema(name = "SQL编辑器配置响应VO")
public class SqlEditConfigRespVO {

    @Schema(name = "配置ID", example = "1")
    private Long id;

    @Schema(name = "SQL编辑器文件ID", example = "1024")
    private Long sqlEditId;

    @Schema(name = "Flink配置信息")
    private FlinkConfig config;

    @Schema(name = "创建时间")
    private LocalDateTime createTime;

    @Schema(name = "更新时间")
    private LocalDateTime updateTime;

}
