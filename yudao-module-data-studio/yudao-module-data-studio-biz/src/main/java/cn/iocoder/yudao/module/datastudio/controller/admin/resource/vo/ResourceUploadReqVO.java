package cn.iocoder.yudao.module.datastudio.controller.admin.resource.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collections;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 数据中台 - 资源上传 Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "数据中台 - 资源上传 Request VO")
@Data
public class ResourceUploadReqVO {

    private static final long MAX_FILE_SIZE = 16 * 1024 * 1024; // 16MB

    private static final String JAR_CONTENT_TYPE = "application/java-archive";

    private static final java.util.List<String> JAR_EXTENSIONS = Collections.singletonList(".jar");

    @Schema(description = "文件附件", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "文件附件不能为空")
    private MultipartFile file;

    @Schema(description = "资源描述")
    private String description;

    /**
     * 校验文件是否为 JAR 类型
     */
    @Schema(hidden = true)
    public boolean isFileValid() {
        if (file == null) {
            return true;
        }
        String filename = file.getOriginalFilename();
        if (filename == null) {
            return false;
        }
        // 校验文件扩展名是否为 .jar
        return JAR_EXTENSIONS.stream().anyMatch(ext -> filename.toLowerCase().endsWith(ext));
    }

    /**
     * 校验文件大小是否超过限制
     */
    @Schema(hidden = true)
    public boolean isSizeValid() {
        if (file == null) {
            return true;
        }
        return file.getSize() <= MAX_FILE_SIZE;
    }

}
