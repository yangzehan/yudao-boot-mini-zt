package cn.iocoder.yudao.module.datastudio.service.resource.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.datastudio.controller.admin.resource.vo.ResourcePageReqVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.resource.vo.ResourceRespVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.resource.vo.ResourceUploadReqVO;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.resource.ResourceDO;
import cn.iocoder.yudao.module.datastudio.dal.mysql.resource.ResourceMapper;
import cn.iocoder.yudao.module.infra.service.file.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.isNull;

/**
 * ResourceServiceImpl 单元测试
 *
 * @author 芋道源码
 */
@ExtendWith(MockitoExtension.class)
class ResourceServiceImplTest {

    @Mock
    private ResourceMapper resourceMapper;

    @Mock
    private FileService fileService;

    @InjectMocks
    private ResourceServiceImpl resourceService;

    private ResourceDO testResource;
    private MultipartFile testFile;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        testResource = new ResourceDO();
        testResource.setId(1L);
        testResource.setName("test-connector.jar");
        testResource.setPath("/data-studio/resource/test-connector.jar");
        testResource.setSize(1024L);
        testResource.setDescription("Test Flink Connector");
        testResource.setFileUrl("http://localhost:8083/test-connector.jar");

        // 初始化测试文件
        byte[] fileContent = "test jar content".getBytes();
        testFile = new MockMultipartFile(
                "file",
                "test-connector.jar",
                "application/java-archive",
                fileContent
        );
    }

    @Test
    @DisplayName("测试上传JAR文件 - 成功")
    void testUploadResource_Success() {
        // 准备参数
        ResourceUploadReqVO uploadReqVO = new ResourceUploadReqVO();
        uploadReqVO.setFile(testFile);
        uploadReqVO.setDescription("Test connector");

        // Mock 文件服务返回路径
        when(fileService.createFile(any(byte[].class), anyString(), anyString(), anyString()))
                .thenReturn("/data-studio/resource/test-connector.jar");

        // Mock Mapper 插入操作
        when(resourceMapper.insert((ResourceDO) any())).thenAnswer(invocation -> {
            ResourceDO resource = invocation.getArgument(0);
            resource.setId(1L);
            return 1;
        });

        // 执行测试
        Long result = resourceService.uploadResource(uploadReqVO);

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result);

        // 验证调用
        verify(fileService).createFile(any(byte[].class), eq("test-connector.jar"), eq("data-studio/resource"), eq("application/java-archive"));
        verify(resourceMapper).insert((ResourceDO) any());
    }

    @Test
    @DisplayName("测试上传非JAR文件 - 失败")
    void testUploadResource_InvalidFileType() {
        // 准备非JAR文件
        MultipartFile invalidFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "test content".getBytes()
        );

        ResourceUploadReqVO uploadReqVO = new ResourceUploadReqVO();
        uploadReqVO.setFile(invalidFile);

        // 执行测试并验证异常
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> resourceService.uploadResource(uploadReqVO)
        );

        assertEquals("仅支持上传 JAR 类型文件", exception.getMessage());

        // 验证没有调用文件服务和Mapper
        verify(fileService, never()).createFile(any(), any(), any(), any());
        verify(resourceMapper, never()).insert((ResourceDO) any());
    }

    @Test
    @DisplayName("测试上传超过大小限制的文件 - 失败")
    void testUploadResource_FileSizeExceeded() {
        // 准备超过16MB的文件
        byte[] largeContent = new byte[17 * 1024 * 1024]; // 17MB
        MultipartFile largeFile = new MockMultipartFile(
                "file",
                "large.jar",
                "application/java-archive",
                largeContent
        );

        ResourceUploadReqVO uploadReqVO = new ResourceUploadReqVO();
        uploadReqVO.setFile(largeFile);

        // 执行测试并验证异常
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> resourceService.uploadResource(uploadReqVO)
        );

        assertEquals("上传文件大小不能超过 16MB", exception.getMessage());

        // 验证没有调用文件服务和Mapper
        verify(fileService, never()).createFile(any(), any(), any(), any());
        verify(resourceMapper, never()).insert((ResourceDO) any());
    }

    @Test
    @DisplayName("测试删除资源 - 成功")
    void testDeleteResource_Success() {
        // Mock 查找资源
        when(resourceMapper.selectById(1L)).thenReturn(testResource);

        // 执行测试
        resourceService.deleteResource(1L);

        // 验证调用
        verify(resourceMapper).selectById(1L);
        verify(resourceMapper).deleteById(1L);
    }

    @Test
    @DisplayName("测试删除不存在的资源 - 失败")
    void testDeleteResource_ResourceNotFound() {
        // Mock 资源不存在
        when(resourceMapper.selectById(999L)).thenReturn(null);

        // 执行测试并验证异常
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> resourceService.deleteResource(999L)
        );

        assertEquals("资源不存在：999", exception.getMessage());

        // 验证没有调用删除
        verify(resourceMapper, never()).deleteById(any());
    }

    @Test
    @DisplayName("测试批量删除资源 - 成功")
    void testDeleteResourceList_Success() {
        // 准备测试数据
        List<ResourceDO> resources = Arrays.asList(testResource);

        // Mock 批量查询
        when(resourceMapper.selectBatchIds(Arrays.asList(1L, 2L))).thenReturn(resources);

        // 执行测试
        resourceService.deleteResourceList(Arrays.asList(1L, 2L));

        // 验证调用
        verify(resourceMapper).selectBatchIds(Arrays.asList(1L, 2L));
        verify(resourceMapper).deleteBatchIds(Arrays.asList(1L, 2L));
    }

    @Test
    @DisplayName("测试批量删除空列表 - 无操作")
    void testDeleteResourceList_EmptyList() {
        // 执行测试
        resourceService.deleteResourceList(Arrays.asList());

        // 验证没有调用Mapper
        verify(resourceMapper, never()).selectBatchIds(anyList());
        verify(resourceMapper, never()).deleteBatchIds(anyList());
    }

    @Test
    @DisplayName("测试批量删除null列表 - 无操作")
    void testDeleteResourceList_NullList() {
        // 执行测试
        resourceService.deleteResourceList(null);

        // 验证没有调用Mapper
        verify(resourceMapper, never()).selectBatchIds(anyList());
        verify(resourceMapper, never()).deleteBatchIds(anyList());
    }

    @Test
    @DisplayName("测试获取资源 - 成功")
    void testGetResource_Success() {
        // Mock 查找资源
        when(resourceMapper.selectById(1L)).thenReturn(testResource);

        // 执行测试
        ResourceDO result = resourceService.getResource(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(testResource.getId(), result.getId());
        assertEquals(testResource.getName(), result.getName());

        // 验证调用
        verify(resourceMapper).selectById(1L);
    }

    @Test
    @DisplayName("测试获取不存在的资源 - 返回null")
    void testGetResource_NotFound() {
        // Mock 资源不存在
        when(resourceMapper.selectById(999L)).thenReturn(null);

        // 执行测试
        ResourceDO result = resourceService.getResource(999L);

        // 验证结果
        assertNull(result);
    }

    @Test
    @DisplayName("测试获取资源列表 - 按名称查询")
    void testGetResourceList_ByName() {
        // 准备测试数据
        List<ResourceDO> resources = Arrays.asList(testResource);

        // Mock 查询
        when(resourceMapper.selectList("connector")).thenReturn(resources);

        // 执行测试
        List<ResourceDO> result = resourceService.getResourceList("connector");

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("test-connector.jar", result.get(0).getName());

        // 验证调用
        verify(resourceMapper).selectList("connector");
    }

    @Test
    @DisplayName("测试获取资源列表 - 空名称查询全部")
    void testGetResourceList_All() {
        // 准备测试数据
        List<ResourceDO> resources = Arrays.asList(testResource);

        // Mock 查询
        when(resourceMapper.selectList((String) isNull())).thenReturn(resources);

        // 执行测试
        List<ResourceDO> result = resourceService.getResourceList(null);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());

        // 验证调用
        verify(resourceMapper).selectList((String) isNull());
    }

    @Test
    @DisplayName("测试获取资源分页 - 成功")
    void testGetResourcePage_Success() {
        // 准备测试数据
        ResourcePageReqVO pageReqVO = new ResourcePageReqVO();
        pageReqVO.setPageNo(1);
        pageReqVO.setPageSize(10);

        List<ResourceDO> resourceList = Arrays.asList(testResource);
        PageResult<ResourceDO> pageResult = new PageResult<>(resourceList, 1L);

        // Mock 分页查询
        when(resourceMapper.selectPage(any(ResourcePageReqVO.class))).thenReturn(pageResult);

        // 执行测试
        PageResult<ResourceRespVO> result = resourceService.getResourcePage(pageReqVO);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getList().size());
        assertEquals("test-connector.jar", result.getList().get(0).getName());

        // 验证调用
        verify(resourceMapper).selectPage(any(ResourcePageReqVO.class));
    }

    @Test
    @DisplayName("测试获取资源分页 - 空结果")
    void testGetResourcePage_EmptyResult() {
        // 准备测试数据
        ResourcePageReqVO pageReqVO = new ResourcePageReqVO();
        pageReqVO.setPageNo(1);
        pageReqVO.setPageSize(10);

        PageResult<ResourceDO> emptyResult = new PageResult<>(Arrays.asList(), 0L);

        // Mock 分页查询
        when(resourceMapper.selectPage(any(ResourcePageReqVO.class))).thenReturn(emptyResult);

        // 执行测试
        PageResult<ResourceRespVO> result = resourceService.getResourcePage(pageReqVO);

        // 验证结果
        assertNotNull(result);
        assertEquals(0, result.getTotal());
        assertEquals(0, result.getList().size());
    }
}
