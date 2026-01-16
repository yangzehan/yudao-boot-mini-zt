package cn.iocoder.yudao.module.datastudio.controller.admin.datasource;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.datastudio.controller.admin.datasource.vo.*;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.datasource.DataSourceDO;
import cn.iocoder.yudao.module.datastudio.service.datasource.DataSourceService;
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
 * 数据源配置 Controller
 *
 * @author 芋道源码
 */
@Tag(name = "数据中台 - 数据源配置")
@RestController
@RequestMapping("/data-studio/datasource")
@Validated
public class DataSourceController {

    @Resource
    private DataSourceService dataSourceService;

    @PostMapping("create")
    @Operation(summary = "创建数据源配置")
    @PreAuthorize("@ss.hasPermission('datastudio:datasource:create')")
    public CommonResult<Long> createDataSource(@Valid @RequestBody DataSourceSaveReqVO createReqVO) {
        Long id = dataSourceService.createDataSource(createReqVO);
        return success(id);
    }

    @PutMapping("update")
    @Operation(summary = "更新数据源配置")
    @PreAuthorize("@ss.hasPermission('datastudio:datasource:update')")
    public CommonResult<Boolean> updateDataSource(@Valid @RequestBody DataSourceSaveReqVO updateReqVO) {
        dataSourceService.updateDataSource(updateReqVO);
        return success(true);
    }

    @DeleteMapping("delete")
    @Operation(summary = "删除数据源配置")
    @Parameter(name = "id", description = "数据源ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('datastudio:datasource:delete')")
    public CommonResult<Boolean> deleteDataSource(@RequestParam("id") Long id) {
        dataSourceService.deleteDataSource(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除数据源配置")
    @Parameter(name = "ids", description = "数据源ID列表", required = true)
    @PreAuthorize("@ss.hasPermission('datastudio:datasource:delete')")
    public CommonResult<Boolean> deleteDataSourceList(@RequestParam("ids") List<Long> ids) {
        dataSourceService.deleteDataSourceList(ids);
        return success(true);
    }

    @GetMapping("/list")
    @Operation(summary = "获取数据源配置列表")
    @PreAuthorize("@ss.hasPermission('datastudio:datasource:query')")
    public CommonResult<List<DataSourceRespVO>> getDataSourceList(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "status", required = false) Integer status) {
        List<DataSourceDO> list = dataSourceService.getDataSourceList(name, type, status);
        return success(BeanUtils.toBean(list, DataSourceRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获取数据源配置分页")
    @PreAuthorize("@ss.hasPermission('datastudio:datasource:query')")
    public CommonResult getDataSourcePage(DataSourcePageReqVO reqVO) {
        return success(dataSourceService.getDataSourcePage(reqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "获取数据源配置")
    @Parameter(name = "id", description = "数据源ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('datastudio:datasource:query')")
    public CommonResult<DataSourceRespVO> getDataSource(@RequestParam("id") Long id) {
        DataSourceDO dataSource = dataSourceService.getDataSource(id);
        return success(BeanUtils.toBean(dataSource, DataSourceRespVO.class));
    }

    @PostMapping("/test-connection")
    @Operation(summary = "测试数据源连接")
    @Parameter(name = "id", description = "数据源ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('datastudio:datasource:test')")
    public CommonResult<ConnectionTestRespVO> testConnection(@RequestParam("id") Long id) {
        ConnectionTestRespVO respVO = new ConnectionTestRespVO();
        try {
            long startTime = System.currentTimeMillis();
            Boolean result = dataSourceService.testConnection(id);
            long responseTime = System.currentTimeMillis() - startTime;

            respVO.setSuccess(result);
            respVO.setResponseTime(responseTime);
            if (result) {
                respVO.setMessage("连接成功");
            } else {
                respVO.setMessage("连接失败");
            }
            return success(respVO);
        } catch (Exception e) {
            respVO.setSuccess(false);
            respVO.setMessage("连接失败：" + e.getMessage());
            return success(respVO);
        }
    }

    @PostMapping("/test-connection-direct")
    @Operation(summary = "测试数据源连接（直接传入配置）")
    @PreAuthorize("@ss.hasPermission('datastudio:datasource:test')")
    public CommonResult<ConnectionTestRespVO> testConnectionDirect(@Valid @RequestBody DataSourceSaveReqVO reqVO) {
        ConnectionTestRespVO respVO = new ConnectionTestRespVO();
        try {
            long startTime = System.currentTimeMillis();
            Boolean result = dataSourceService.testConnection(reqVO);
            long responseTime = System.currentTimeMillis() - startTime;

            respVO.setSuccess(result);
            respVO.setResponseTime(responseTime);
            if (result) {
                respVO.setMessage("连接成功");
            } else {
                respVO.setMessage("连接失败");
            }
            return success(respVO);
        } catch (Exception e) {
            respVO.setSuccess(false);
            respVO.setMessage("连接失败：" + e.getMessage());
            return success(respVO);
        }
    }

    @GetMapping("/types")
    @Operation(summary = "获取数据源类型列表")
    @PreAuthorize("@ss.hasPermission('datastudio:datasource:query')")
    public CommonResult<List<DataSourceTypeRespVO>> getDataSourceTypes() {
        List<DataSourceTypeRespVO> types = dataSourceService.getDataSourceTypes();
        return success(types);
    }

    @GetMapping("/tables")
    @Operation(summary = "获取数据源的表列表")
    @Parameter(name = "datasourceId", description = "数据源ID", required = true)
    public CommonResult<List<String>> getTables(@RequestParam("datasourceId") Long datasourceId) {
        return success(dataSourceService.getTables(datasourceId));
    }

}
