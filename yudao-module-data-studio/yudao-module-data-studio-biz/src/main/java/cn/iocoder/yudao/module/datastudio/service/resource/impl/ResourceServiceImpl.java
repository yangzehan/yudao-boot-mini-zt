package cn.iocoder.yudao.module.datastudio.service.resource.impl;

import cn.hutool.core.io.IoUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.datastudio.controller.admin.resource.vo.ResourcePageReqVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.resource.vo.ResourceRespVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.resource.vo.ResourceUploadReqVO;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.resource.ResourceDO;
import cn.iocoder.yudao.module.datastudio.dal.mysql.resource.ResourceMapper;
import cn.iocoder.yudao.module.datastudio.service.resource.ResourceService;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileDO;
import cn.iocoder.yudao.module.infra.dal.mysql.file.FileMapper;
import cn.iocoder.yudao.module.infra.service.file.FileService;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.hutool.core.util.StrUtil;
import java.io.IOException;
import java.util.List;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * 数据中台 - 资源 Service 实现
 *
 * @author 芋道源码
 */
@Slf4j
@Service
public class ResourceServiceImpl implements ResourceService {

  @Resource private ResourceMapper resourceMapper;

  @Resource private FileService fileService;

  @Resource private FileMapper fileMapper;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Long uploadResource(ResourceUploadReqVO uploadReqVO) {
    MultipartFile file = uploadReqVO.getFile();

    // 1. 校验文件类型
    String filename = file.getOriginalFilename();
    if (filename == null || !filename.toLowerCase().endsWith(".jar")) {
      throw new IllegalArgumentException("仅支持上传 JAR 类型文件");
    }

    // 2. 校验文件大小
    long fileSize = file.getSize();
    if (fileSize > 16 * 1024 * 1024) {
      throw new IllegalArgumentException("上传文件大小不能超过 16MB");
    }

    try {
      // 3. 上传文件到文件服务
      byte[] content = IoUtil.readBytes(file.getInputStream());
      String filePath =
          fileService.createFile(content, filename, "data-studio/resource", file.getContentType());

      // 4. 保存资源记录
      ResourceDO resource = new ResourceDO();
      resource.setName(filename);
      resource.setPath(filePath);
      resource.setSize(fileSize);
      resource.setDescription(uploadReqVO.getDescription());
      resource.setFileUrl(filePath);
      resourceMapper.insert(resource);

      log.info("资源上传成功：{}，大小：{}字节", filename, fileSize);
      return resource.getId();

    } catch (IOException e) {
      log.error("资源上传失败：{}", filename, e);
      throw new RuntimeException("资源上传失败：" + e.getMessage());
    }
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void deleteResource(Long id) {
    ResourceDO resource = resourceMapper.selectById(id);
    if (resource == null) {
      throw new IllegalArgumentException("资源不存在：" + id);
    }

    // 删除文件记录
    deleteFileByPath(resource.getFileUrl());

    // 删除资源记录
    resourceMapper.deleteById(id);
    log.info("资源删除成功：{}", resource.getName());
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void deleteResourceList(List<Long> ids) {
    if (CollectionUtils.isEmpty(ids)) {
      return;
    }

    List<ResourceDO> resources = resourceMapper.selectBatchIds(ids);
    // 删除所有关联的文件
    resources.forEach(resource -> deleteFileByPath(resource.getFileUrl()));
    // 删除资源记录
    resourceMapper.deleteBatchIds(ids);
    resources.forEach(resource -> log.info("资源删除成功：{}", resource.getName()));
  }

  /**
   * 根据文件路径删除文件记录
   *
   * @param fileUrl 文件路径
   */
  private void deleteFileByPath(String fileUrl) {
    if (StrUtil.isBlank(fileUrl)) {
      return;
    }
    FileDO fileDO = fileMapper.selectOne(new LambdaQueryWrapperX<FileDO>().eq(FileDO::getUrl, fileUrl));
    if (fileDO != null) {
      try {
        fileService.deleteFile(fileDO.getId());
      } catch (Exception e) {
        log.error("删除文件失败：{}", fileUrl, e);
      }
    }
  }

  @Override
  public ResourceDO getResource(Long id) {
    return resourceMapper.selectById(id);
  }

  @Override
  public List<ResourceDO> getResourceList(String name) {
    return resourceMapper.selectList(name);
  }

  @Override
  public PageResult<ResourceRespVO> getResourcePage(ResourcePageReqVO pageReqVO) {
    PageResult<ResourceDO> pageResult = resourceMapper.selectPage(pageReqVO);
    return BeanUtils.toBean(pageResult, ResourceRespVO.class);
  }
}
