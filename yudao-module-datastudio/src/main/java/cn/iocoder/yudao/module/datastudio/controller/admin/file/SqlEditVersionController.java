package cn.iocoder.yudao.module.datastudio.controller.admin.file;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.datastudio.controller.admin.file.vo.version.*;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.file.SqlEditVersionDO;
import cn.iocoder.yudao.module.datastudio.service.file.SqlEditVersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SQL编辑器版本 Controller
 *
 * @author 芋道源码
 */
@Tag(name = "数据中台 - SQL 编辑器版本")
@RestController
@RequestMapping("/sql-edit-version")
@Validated
public class SqlEditVersionController {

    @Resource
    private SqlEditVersionService versionService;

    @Operation(summary = "获取版本列表")
    @GetMapping("/list")
    public CommonResult<PageResult<SqlEditVersionRespVO>> getVersionList(@Valid SqlEditVersionListReqVO reqVO) {
        PageResult<SqlEditVersionDO> pageResult = versionService.getVersionList(reqVO.getSqlEditId(), reqVO);
        List<SqlEditVersionRespVO> voList = pageResult.getList().stream()
                .map(SqlEditVersionRespVO::of)
                .collect(Collectors.toList());
        return CommonResult.success(PageResult.of(voList, pageResult.getTotal()));
    }

    @Operation(summary = "获取版本详情")
    @GetMapping("/{id}")
    public CommonResult<SqlEditVersionDetailRespVO> getVersionDetail(@PathVariable Long id) {
        SqlEditVersionDO version = versionService.getVersionDetail(id);
        return CommonResult.success(SqlEditVersionDetailRespVO.of(version));
    }

    @Operation(summary = "回退到指定版本")
    @PostMapping("/rollback/{id}")
    public CommonResult<Boolean> rollbackVersion(@PathVariable Long id) {
        Boolean result = versionService.rollbackToVersion(id);
        return CommonResult.success(result);
    }

    @Operation(summary = "删除版本")
    @DeleteMapping("/{id}")
    public CommonResult<Boolean> deleteVersion(@PathVariable Long id) {
        Boolean result = versionService.deleteVersion(id);
        return CommonResult.success(result);
    }

}
