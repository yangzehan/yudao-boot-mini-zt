package cn.iocoder.yudao.module.datastudio.controller.admin.file;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.datastudio.controller.admin.file.vo.file.FileManageListReqVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.file.vo.resp.FileManageRespVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.file.vo.save.FileManageSaveReqVO;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.file.FileManageDO;
import cn.iocoder.yudao.module.datastudio.service.file.FileManageService;
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
 * 文件管理 Controller
 *
 * @author 芋道源码
 */
@Tag(name = "数据中台 - 文件管理")
@RestController
@RequestMapping("/data-studio/file")
@Validated
public class FileManageController {

    @Resource
    private FileManageService fileManageService;

    @PostMapping("create")
    @Operation(summary = "创建文件/文件夹")
    @PreAuthorize("@ss.hasPermission('datastudio:file:create')")
    public CommonResult<Long> createFile(@Valid @RequestBody FileManageSaveReqVO createReqVO) {
        Long fileId = fileManageService.createFile(createReqVO);
        return success(fileId);
    }

    @PutMapping("update")
    @Operation(summary = "更新文件/文件夹")
    @PreAuthorize("@ss.hasPermission('datastudio:file:update')")
    public CommonResult<Boolean> updateFile(@Valid @RequestBody FileManageSaveReqVO updateReqVO) {
        fileManageService.updateFile(updateReqVO);
        return success(true);
    }

    @DeleteMapping("delete")
    @Operation(summary = "删除文件/文件夹")
    @Parameter(name = "id", description = "文件ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('datastudio:file:delete')")
    public CommonResult<Boolean> deleteFile(@RequestParam("id") Long id) {
        fileManageService.deleteFile(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除文件/文件夹")
    @Parameter(name = "ids", description = "文件ID列表", required = true)
    @PreAuthorize("@ss.hasPermission('datastudio:file:delete')")
    public CommonResult<Boolean> deleteFileList(@RequestParam("ids") List<Long> ids) {
        fileManageService.deleteFileList(ids);
        return success(true);
    }

    @GetMapping("/list")
    @Operation(summary = "获取文件列表")
    @PreAuthorize("@ss.hasPermission('datastudio:file:query')")
    public CommonResult<List<FileManageRespVO>> getFileList(FileManageListReqVO reqVO) {
        List<FileManageDO> list = fileManageService.getFileList(reqVO);
        return success(BeanUtils.toBean(list, FileManageRespVO.class));
    }

    @GetMapping("/tree")
    @Operation(summary = "获取文件树形结构")
    @PreAuthorize("@ss.hasPermission('datastudio:file:query')")
    public CommonResult<List<FileManageRespVO>> getFileTree() {
        return success(fileManageService.getFileTree());
    }

    @GetMapping("/children")
    @Operation(summary = "获取指定目录下的子文件列表")
    @PreAuthorize("@ss.hasPermission('datastudio:file:query')")
    public CommonResult<List<FileManageRespVO>> getFilesByParentId(@RequestParam("parentId") Long parentId) {
        return success(fileManageService.getFilesByParentId(parentId));
    }

    @GetMapping("/search")
    @Operation(summary = "搜索文件")
    @PreAuthorize("@ss.hasPermission('datastudio:file:query')")
    public CommonResult<List<FileManageRespVO>> searchFiles(@RequestParam("keyword") String keyword) {
        return success(fileManageService.searchFiles(keyword));
    }

    @GetMapping("/get")
    @Operation(summary = "获取文件信息")
    @Parameter(name = "id", description = "文件ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('datastudio:file:query')")
    public CommonResult<FileManageRespVO> getFile(@RequestParam("id") Long id) {
        FileManageDO file = fileManageService.getFile(id);
        return success(BeanUtils.toBean(file, FileManageRespVO.class));
    }

    @PostMapping("/move")
    @Operation(summary = "移动文件")
    @PreAuthorize("@ss.hasPermission('datastudio:file:update')")
    public CommonResult<Boolean> moveFile(@RequestParam("id") Long id,
                                          @RequestParam("targetParentId") Long targetParentId) {
        fileManageService.moveFile(id, targetParentId);
        return success(true);
    }

    @PostMapping("/rename")
    @Operation(summary = "重命名文件")
    @PreAuthorize("@ss.hasPermission('datastudio:file:update')")
    public CommonResult<Boolean> renameFile(@RequestParam("id") Long id,
                                           @RequestParam("name") String name) {
        fileManageService.renameFile(id, name);
        return success(true);
    }

    @PostMapping("/save-content")
    @Operation(summary = "保存文件内容")
    @PreAuthorize("@ss.hasPermission('datastudio:file:update')")
    public CommonResult<Boolean> saveFileContent(@RequestParam("id") Long id,
                                                @RequestParam("content") String content) {
        fileManageService.saveFileContent(id, content);
        return success(true);
    }

    @GetMapping("/get-content")
    @Operation(summary = "获取文件内容")
    @PreAuthorize("@ss.hasPermission('datastudio:file:query')")
    public CommonResult<String> getFileContent(@RequestParam("id") Long id) {
        String content = fileManageService.getFileContent(id);
        return success(content);
    }

}
