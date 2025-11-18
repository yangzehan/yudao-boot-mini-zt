package cn.iocoder.yudao.module.datastudio.controller.admin.dataIngestion;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.datastudio.controller.admin.dataIngestion.vo.file.DataIngestionListReqVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.dataIngestion.vo.resp.DataIngestionRespVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.dataIngestion.vo.save.DataIngestionSaveReqVO;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.dataIngestion.DataIngestionDO;
import cn.iocoder.yudao.module.datastudio.service.dataIngestion.DataIngestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 数据摄取 Controller
 *
 * @author 芋道源码
 */
@Tag(name = "数据中台 - 数据摄取")
@RestController
@RequestMapping("/data-ingestion")
@Validated
public class DataIngestionController {

    @Resource
    private DataIngestionService dataIngestionService;

    @PostMapping("create")
    @Operation(summary = "创建文件/文件夹")
    @PreAuthorize("@ss.hasPermission('datastudio:file:create')")
    public CommonResult<Long> createFile(@Valid @RequestBody DataIngestionSaveReqVO createReqVO) {
        Long fileId = dataIngestionService.createFile(createReqVO);
        return success(fileId);
    }

    @PutMapping("update")
    @Operation(summary = "更新文件/文件夹")
    @PreAuthorize("@ss.hasPermission('datastudio:file:update')")
    public CommonResult<Boolean> updateFile(@Valid @RequestBody DataIngestionSaveReqVO updateReqVO) {
        dataIngestionService.updateFile(updateReqVO);
        return success(true);
    }

    @DeleteMapping("delete")
    @Operation(summary = "删除文件/文件夹")
    @Parameter(name = "id", description = "文件ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('datastudio:file:delete')")
    public CommonResult<Boolean> deleteFile(@RequestParam("id") Long id) {
        dataIngestionService.deleteFile(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除文件/文件夹")
    @Parameter(name = "ids", description = "文件ID列表", required = true)
    @PreAuthorize("@ss.hasPermission('datastudio:file:delete')")
    public CommonResult<Boolean> deleteFileList(@RequestParam("ids") List<Long> ids) {
        dataIngestionService.deleteFileList(ids);
        return success(true);
    }

    @GetMapping("/list")
    @Operation(summary = "获取文件列表")
    @PreAuthorize("@ss.hasPermission('datastudio:file:query')")
    public CommonResult<List<DataIngestionRespVO>> getFileList(DataIngestionListReqVO reqVO) {
        List<DataIngestionDO> list = dataIngestionService.getFileList(reqVO);
        return success(BeanUtils.toBean(list, DataIngestionRespVO.class));
    }

    @GetMapping("/tree")
    @Operation(summary = "获取文件树形结构")
    @PreAuthorize("@ss.hasPermission('datastudio:file:query')")
    public CommonResult<List<DataIngestionRespVO>> getFileTree() {
        return success(dataIngestionService.getFileTree());
    }

    @GetMapping("/children")
    @Operation(summary = "获取指定目录下的子文件列表")
    @PreAuthorize("@ss.hasPermission('datastudio:file:query')")
    public CommonResult<List<DataIngestionRespVO>> getFilesByParentId(@RequestParam("parentId") Long parentId) {
        return success(dataIngestionService.getFilesByParentId(parentId));
    }

    @GetMapping("/search")
    @Operation(summary = "搜索文件")
    @PreAuthorize("@ss.hasPermission('datastudio:file:query')")
    public CommonResult<List<DataIngestionRespVO>> searchFiles(@RequestParam("keyword") String keyword) {
        return success(dataIngestionService.searchFiles(keyword));
    }

    @GetMapping("/get")
    @Operation(summary = "获取文件信息")
    @Parameter(name = "id", description = "文件ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('datastudio:file:query')")
    public CommonResult<DataIngestionRespVO> getFile(@RequestParam("id") Long id) {
        DataIngestionDO file = dataIngestionService.getFile(id);
        return success(BeanUtils.toBean(file, DataIngestionRespVO.class));
    }

    @PostMapping("/move")
    @Operation(summary = "移动文件")
    @PreAuthorize("@ss.hasPermission('datastudio:file:update')")
    public CommonResult<Boolean> moveFile(@RequestParam("id") Long id,
                                          @RequestParam("targetParentId") Long targetParentId) {
        dataIngestionService.moveFile(id, targetParentId);
        return success(true);
    }

    @PostMapping("/rename")
    @Operation(summary = "重命名文件")
    @PreAuthorize("@ss.hasPermission('datastudio:file:update')")
    public CommonResult<Boolean> renameFile(@RequestParam("id") Long id,
                                           @RequestParam("name") String name) {
        dataIngestionService.renameFile(id, name);
        return success(true);
    }

    @PostMapping("/save-content")
    @Operation(summary = "保存文件内容")
    @PreAuthorize("@ss.hasPermission('datastudio:file:update')")
    public CommonResult<Boolean> saveFileContent(@RequestParam("id") Long id,
                                                @RequestParam("content") String content) {
        dataIngestionService.saveFileContent(id, content);
        return success(true);
    }

    @GetMapping("/get-content")
    @Operation(summary = "获取文件内容")
    @PreAuthorize("@ss.hasPermission('datastudio:file:query')")
    public CommonResult<String> getFileContent(@RequestParam("id") Long id) {
        String content = dataIngestionService.getFileContent(id);
        return success(content);
    }

}
