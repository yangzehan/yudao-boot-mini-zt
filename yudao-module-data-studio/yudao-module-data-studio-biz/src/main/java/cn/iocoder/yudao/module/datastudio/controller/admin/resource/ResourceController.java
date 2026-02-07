package cn.iocoder.yudao.module.datastudio.controller.admin.resource;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.datastudio.controller.admin.resource.vo.ResourcePageReqVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.resource.vo.ResourceRespVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.resource.vo.ResourceUploadReqVO;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.resource.ResourceDO;
import cn.iocoder.yudao.module.datastudio.service.resource.ResourceService;
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
 * 数据中台 - 资源 Controller
 *
 * @author 芋道源码
 */
@Tag(name = "数据中台 - 资源管理")
@RestController
@RequestMapping("/datastudio/resource")
@Validated
public class ResourceController {

    @Resource
    private ResourceService resourceService;

    @PostMapping("/upload")
    @Operation(summary = "上传资源", description = "仅支持上传 JAR 类型文件，大小限制 16MB")
    @PreAuthorize("@ss.hasPermission('datastudio:resource:upload')")
    public CommonResult<Long> uploadResource(@Valid ResourceUploadReqVO uploadReqVO) {
        Long id = resourceService.uploadResource(uploadReqVO);
        return success(id);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除资源")
    @Parameter(name = "id", description = "资源ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('datastudio:resource:delete')")
    public CommonResult<Boolean> deleteResource(@RequestParam("id") Long id) {
        resourceService.deleteResource(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除资源")
    @Parameter(name = "ids", description = "资源ID列表", required = true)
    @PreAuthorize("@ss.hasPermission('datastudio:resource:delete')")
    public CommonResult<Boolean> deleteResourceList(@RequestParam("ids") List<Long> ids) {
        resourceService.deleteResourceList(ids);
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获取资源分页")
    @PreAuthorize("@ss.hasPermission('datastudio:resource:query')")
    public CommonResult<PageResult<ResourceRespVO>> getResourcePage(@Valid ResourcePageReqVO reqVO) {
        return success(resourceService.getResourcePage(reqVO));
    }

    @GetMapping("/list")
    @Operation(summary = "获取资源列表")
    @PreAuthorize("@ss.hasPermission('datastudio:resource:query')")
    public CommonResult<List<ResourceRespVO>> getResourceList(
            @RequestParam(value = "name", required = false) String name) {
        List<ResourceDO> list = resourceService.getResourceList(name);
        return success(BeanUtils.toBean(list, ResourceRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获取资源")
    @Parameter(name = "id", description = "资源ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('datastudio:resource:query')")
    public CommonResult<ResourceRespVO> getResource(@RequestParam("id") Long id) {
        ResourceDO resource = resourceService.getResource(id);
        return success(BeanUtils.toBean(resource, ResourceRespVO.class));
    }

}
