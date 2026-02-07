package cn.iocoder.yudao.module.datastudio.service.resource;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.datastudio.controller.admin.resource.vo.ResourcePageReqVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.resource.vo.ResourceRespVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.resource.vo.ResourceUploadReqVO;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.resource.ResourceDO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 数据中台 - 资源 Service 接口
 *
 * @author 芋道源码
 */
public interface ResourceService {

    /**
     * 上传资源文件
     *
     * @param uploadReqVO 上传请求
     * @return 资源ID
     */
    Long uploadResource(ResourceUploadReqVO uploadReqVO);

    /**
     * 删除资源
     *
     * @param id 资源ID
     */
    void deleteResource(Long id);

    /**
     * 批量删除资源
     *
     * @param ids 资源ID列表
     */
    void deleteResourceList(List<Long> ids);

    /**
     * 获得资源
     *
     * @param id 资源ID
     * @return 资源
     */
    ResourceDO getResource(Long id);

    /**
     * 获得资源列表
     *
     * @param name 资源名称
     * @return 资源列表
     */
    List<ResourceDO> getResourceList(String name);

    /**
     * 获得资源分页
     *
     * @param pageReqVO 分页查询
     * @return 资源分页
     */
    PageResult<ResourceRespVO> getResourcePage(ResourcePageReqVO pageReqVO);

}
