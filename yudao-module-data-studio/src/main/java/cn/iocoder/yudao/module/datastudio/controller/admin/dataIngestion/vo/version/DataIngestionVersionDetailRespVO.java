package cn.iocoder.yudao.module.datastudio.controller.admin.dataIngestion.vo.version;

import cn.iocoder.yudao.module.datastudio.dal.dataobject.dataIngestion.DataIngestionVersionDO;
import cn.iocoder.yudao.module.flink.common.dto.FlinkConfig;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据摄取版本详情响应 VO
 *
 * @author 芋道源码
 */
@Data
@Schema(name = "数据摄取版本详情响应 VO")
public class DataIngestionVersionDetailRespVO {

    @Schema(name = "版本ID", required = true, example = "1")
    private Long id;

    @Schema(name = "文件ID", required = true, example = "1")
    private Long dataIngestionId;

    @Schema(name = "版本号", required = true, example = "1")
    private Long versionNumber;

    @Schema(name = "文件内容", required = true)
    private String content;

    @Schema(name = "配置信息", required = true)
    private FlinkConfig config;

    @Schema(name = "版本备注", example = "这是版本备注")
    private String remark;

    @Schema(name = "版本类型", example = "manual-手动保存, auto-自动保存")
    private String versionType;

    @Schema(name = "创建时间", required = true)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(name = "创建者", required = true)
    private String creator;

    public static DataIngestionVersionDetailRespVO of(DataIngestionVersionDO versionDO) {
        DataIngestionVersionDetailRespVO respVO = new DataIngestionVersionDetailRespVO();
        respVO.setId(versionDO.getId());
        respVO.setDataIngestionId(versionDO.getDataIngestionId());
        respVO.setVersionNumber(versionDO.getVersionNumber());
        respVO.setContent(versionDO.getContent());
        respVO.setConfig(versionDO.getConfig());
        respVO.setRemark(versionDO.getRemark());
        respVO.setVersionType(versionDO.getVersionType());
        respVO.setCreateTime(versionDO.getCreateTime());
        respVO.setCreator(versionDO.getCreator());
        return respVO;
    }

}
