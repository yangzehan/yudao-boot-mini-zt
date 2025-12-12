package cn.iocoder.yudao.module.datastudio.controller.admin.flinkcluster;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.datastudio.controller.admin.flinkcluster.vo.extra.FlinkClusterConnectionTestRespVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.flinkcluster.vo.extra.FlinkClusterStatisticsRespVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.flinkcluster.vo.extra.FlinkClusterTypeRespVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.flinkcluster.vo.extra.FlinkClusterVersionRespVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.flinkcluster.vo.list.FlinkClusterListReqVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.flinkcluster.vo.list.FlinkClusterPageReqVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.flinkcluster.vo.resp.FlinkClusterRespVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.flinkcluster.vo.save.FlinkClusterBatchDeleteReqVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.flinkcluster.vo.save.FlinkClusterSaveReqVO;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.flinkcluster.FlinkClusterDO;
import cn.iocoder.yudao.module.datastudio.service.flinkcluster.FlinkClusterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * Flink 集群配置 Controller
 *
 * @author 芋道源码
 */
@Slf4j
@Tag(name = "数据中台 - Flink 集群配置")
@RestController
@RequestMapping("/data-studio/flink-cluster")
@Validated
public class FlinkClusterController {

    @Resource
    private FlinkClusterService flinkClusterService;

    @PostMapping("create")
    @Operation(summary = "创建 Flink 集群配置")
    @PreAuthorize("@ss.hasPermission('datastudio:flink-cluster:create')")
    public CommonResult<Long> createFlinkCluster(@Valid @RequestBody FlinkClusterSaveReqVO createReqVO) {
        Long id = flinkClusterService.createFlinkCluster(createReqVO);
        return success(id);
    }

    @PutMapping("update")
    @Operation(summary = "更新 Flink 集群配置")
    @PreAuthorize("@ss.hasPermission('datastudio:flink-cluster:update')")
    public CommonResult<Boolean> updateFlinkCluster(@Valid @RequestBody FlinkClusterSaveReqVO updateReqVO) {
        flinkClusterService.updateFlinkCluster(updateReqVO);
        return success(true);
    }

    @DeleteMapping("delete")
    @Operation(summary = "删除 Flink 集群配置")
    @Parameter(name = "id", description = "集群ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('datastudio:flink-cluster:delete')")
    public CommonResult<Boolean> deleteFlinkCluster(@RequestParam("id") Long id) {
        flinkClusterService.deleteFlinkCluster(id);
        return success(true);
    }

    @PostMapping("/batch-delete")
    @Operation(summary = "批量删除 Flink 集群配置")
    @PreAuthorize("@ss.hasPermission('datastudio:flink-cluster:delete')")
    public CommonResult<Boolean> deleteFlinkClusterList(@Valid @RequestBody FlinkClusterBatchDeleteReqVO deleteReqVO) {
        flinkClusterService.deleteFlinkClusterList(deleteReqVO);
        return success(true);
    }

    @GetMapping("/list")
    @Operation(summary = "获取 Flink 集群配置列表")
    @PreAuthorize("@ss.hasPermission('datastudio:flink-cluster:query')")
    public CommonResult<List<FlinkClusterRespVO>> getFlinkClusterList(FlinkClusterListReqVO listReqVO) {
        List<FlinkClusterDO> list = flinkClusterService.getFlinkClusterList(listReqVO);
        return success(BeanUtils.toBean(list, FlinkClusterRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获取 Flink 集群配置分页")
    @PreAuthorize("@ss.hasPermission('datastudio:flink-cluster:query')")
    public CommonResult<PageResult<FlinkClusterRespVO>> getFlinkClusterPage(FlinkClusterPageReqVO pageReqVO) {
        PageResult<FlinkClusterDO> pageResult = flinkClusterService.getFlinkClusterPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, FlinkClusterRespVO.class));
    }

    @GetMapping("/detail/{id}")
    @Operation(summary = "获取 Flink 集群配置详情")
    @Parameter(name = "id", description = "集群ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('datastudio:flink-cluster:query')")
    public CommonResult<FlinkClusterRespVO> getFlinkCluster(@Parameter(name = "id") @PathVariable("id") Long id) {
        FlinkClusterDO cluster = flinkClusterService.getFlinkCluster(id);
        return success(BeanUtils.toBean(cluster, FlinkClusterRespVO.class));
    }

    @PostMapping("/test-connection")
    @Operation(summary = "测试集群连接")
    @Parameter(name = "id", description = "集群ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('datastudio:flink-cluster:test')")
    public CommonResult<FlinkClusterConnectionTestRespVO> testConnection(@RequestParam("id") Long id) {
        FlinkClusterConnectionTestRespVO result = flinkClusterService.testConnection(id);
        return success(result);
    }

    @PostMapping("/test-form")
    @Operation(summary = "测试集群配置（不保存到数据库）")
    @Parameter(name = "createReqVO", description = "集群配置", required = true)
    @PreAuthorize("@ss.hasPermission('datastudio:flink-cluster:create')")
    public CommonResult<FlinkClusterConnectionTestRespVO> testForm(@Valid @RequestBody FlinkClusterSaveReqVO createReqVO) {
        FlinkClusterConnectionTestRespVO result = flinkClusterService.testConnection(createReqVO);
        return success(result);
    }

    @PostMapping("/refresh-status")
    @Operation(summary = "刷新集群状态")
    @Parameter(name = "id", description = "集群ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('datastudio:flink-cluster:update')")
    public CommonResult<FlinkClusterDO> refreshStatus(@RequestParam("id") Long id) {
        FlinkClusterDO cluster = flinkClusterService.refreshStatus(id);
        return success(cluster);
    }

    @GetMapping("/types")
    @Operation(summary = "获取集群类型列表")
    @PreAuthorize("@ss.hasPermission('datastudio:flink-cluster:query')")
    public CommonResult<List<FlinkClusterTypeRespVO>> getClusterTypes() {
        List<FlinkClusterTypeRespVO> types = flinkClusterService.getClusterTypes();
        return success(types);
    }

    @GetMapping("/flink-versions")
    @Operation(summary = "获取 Flink 版本列表")
    @PreAuthorize("@ss.hasPermission('datastudio:flink-cluster:query')")
    public CommonResult<List<FlinkClusterVersionRespVO>> getFlinkVersions() {
        List<FlinkClusterVersionRespVO> versions = flinkClusterService.getFlinkVersions();
        return success(versions);
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取集群统计信息")
    @PreAuthorize("@ss.hasPermission('datastudio:flink-cluster:query')")
    public CommonResult<FlinkClusterStatisticsRespVO> getStatistics() {
        FlinkClusterStatisticsRespVO statistics = flinkClusterService.getStatistics();
        return success(statistics);
    }

    @GetMapping("/export")
    @Operation(summary = "导出集群配置")
    @PreAuthorize("@ss.hasPermission('datastudio:flink-cluster:export')")
    public CommonResult<String> exportClusters(
            @RequestParam(value = "ids", required = false) String ids,
            @RequestParam(value = "format", defaultValue = "json") String format) {
        // TODO: 实现导出功能
        return success("导出功能待实现");
    }

    @PostMapping("/import")
    @Operation(summary = "导入集群配置")
    @PreAuthorize("@ss.hasPermission('datastudio:flink-cluster:import')")
    public CommonResult<String> importClusters() {
        // TODO: 实现导入功能
        return success("导入功能待实现");
    }

}
