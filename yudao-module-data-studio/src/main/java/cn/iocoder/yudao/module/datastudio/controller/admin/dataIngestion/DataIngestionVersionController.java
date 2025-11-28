package cn.iocoder.yudao.module.datastudio.controller.admin.dataIngestion;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.datastudio.controller.admin.dataIngestion.vo.version.*;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.dataIngestion.DataIngestionVersionDO;
import cn.iocoder.yudao.module.datastudio.service.dataIngestionVersion.DataIngestionVersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据摄取版本 Controller
 *
 * @author 芋道源码
 */
@Tag(name = "数据中台 - 数据摄取版本")
@RestController
@RequestMapping("/data-ingestion-version")
@Validated
public class DataIngestionVersionController {

    @Resource
    private DataIngestionVersionService versionService;

    @Operation(summary = "获取版本列表")
    @GetMapping("/list")
    @PreAuthorize("@ss.hasPermission('datastudio:dataIngestion:query')")
    public CommonResult<PageResult<DataIngestionVersionRespVO>> getVersionList(@Valid DataIngestionVersionListReqVO reqVO) {
        PageResult<DataIngestionVersionDO> pageResult = versionService.getVersionList(reqVO.getDataIngestionId(), reqVO);
        List<DataIngestionVersionRespVO> voList = pageResult.getList().stream()
                .map(DataIngestionVersionRespVO::of)
                .collect(Collectors.toList());
        return CommonResult.success(new PageResult<>(voList, pageResult.getTotal()));
    }

    @Operation(summary = "获取版本详情")
    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasPermission('datastudio:dataIngestion:query')")
    public CommonResult<DataIngestionVersionDetailRespVO> getVersionDetail(@PathVariable Long id) {
        DataIngestionVersionDO version = versionService.getVersionDetail(id);
        return CommonResult.success(DataIngestionVersionDetailRespVO.of(version));
    }

    @Operation(summary = "回退到指定版本")
    @PostMapping("/rollback/{id}")
    @PreAuthorize("@ss.hasPermission('datastudio:dataIngestion:update')")
    public CommonResult<Boolean> rollbackVersion(@PathVariable Long id) {
        Boolean result = versionService.rollbackToVersion(id);
        return CommonResult.success(result);
    }

    @Operation(summary = "删除版本")
    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPermission('datastudio:dataIngestion:delete')")
    public CommonResult<Boolean> deleteVersion(@PathVariable Long id) {
        Boolean result = versionService.deleteVersion(id);
        return CommonResult.success(result);
    }

    @Operation(summary = "创建版本")
    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('datastudio:dataIngestion:create')")
    public CommonResult<Long> createVersion(@Valid @RequestBody DataIngestionVersionCreateReqVO reqVO) {
        Long versionId = versionService.createVersion(
            reqVO.getDataIngestionId(),
            reqVO.getContent(),
            reqVO.getConfig(),
            reqVO.getRemark(),
            reqVO.getVersionType()
        );
        return CommonResult.success(versionId);
    }

}
