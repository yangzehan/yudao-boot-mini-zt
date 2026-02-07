package cn.iocoder.yudao.module.datastudio.controller.admin.sql.vo.version;

import cn.iocoder.yudao.module.datastudio.dal.dataobject.file.SqlEditVersionDO;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * SQL编辑器版本响应 VO
 *
 * @author 芋道源码
 */
@Data
@Schema(name = "SQL编辑器版本响应 VO")
public class SqlEditVersionRespVO {

  @Schema(name = "版本ID", required = true, example = "1")
  private Long id;

  @Schema(name = "文件ID", required = true, example = "1")
  private Long sqlEditId;

  @Schema(name = "版本号", required = true, example = "1")
  private Long versionNumber;

  @Schema(name = "版本备注", example = "这是版本备注")
  private String remark;

  @Schema(name = "版本类型", example = "manual-手动保存, auto-自动保存")
  private String versionType;

  @Schema(name = "创建时间", required = true)
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createTime;

  @Schema(name = "创建者", required = true)
  private String creator;

  public static SqlEditVersionRespVO of(SqlEditVersionDO versionDO) {
    SqlEditVersionRespVO respVO = new SqlEditVersionRespVO();
    respVO.setId(versionDO.getId());
    respVO.setSqlEditId(versionDO.getSqlEditId());
    respVO.setVersionNumber(versionDO.getVersionNumber());
    respVO.setRemark(versionDO.getRemark());
    respVO.setVersionType(versionDO.getVersionType());
    respVO.setCreateTime(versionDO.getCreateTime());
    respVO.setCreator(versionDO.getCreator());
    return respVO;
  }
}
