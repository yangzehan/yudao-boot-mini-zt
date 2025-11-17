package cn.iocoder.yudao.module.datastudio.service.file;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.datastudio.controller.admin.file.vo.file.FileManageListReqVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.file.vo.save.FileManageSaveReqVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.file.vo.resp.FileManageRespVO;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.file.FileManageDO;
import cn.iocoder.yudao.module.datastudio.dal.mysql.file.FileManageMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 文件管理 Service 实现
 *
 * @author 芋道源码
 */
@Service
public class FileManageServiceImpl implements FileManageService {

    @Resource
    private FileManageMapper fileManageMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createFile(FileManageSaveReqVO createReqVO) {
        // 获取当前租户ID
        Long tenantId = TenantContextHolder.getTenantId();

        // 验证父文件夹是否存在
        if (createReqVO.getParentId() != null && createReqVO.getParentId() > 0) {
            FileManageDO parent = fileManageMapper.selectById(createReqVO.getParentId());
            if (parent == null) {
                throw new IllegalArgumentException("父文件夹不存在");
            }
            // 验证父文件夹属于当前租户
            if (!tenantId.equals(parent.getTenantId())) {
                throw new IllegalArgumentException("无权访问该父文件夹");
            }
        }

        FileManageDO file = BeanUtils.toBean(createReqVO, FileManageDO.class);
        // 确保设置租户ID
        if (file.getTenantId() == null) {
            file.setTenantId(tenantId);
        }
        // 如果是创建根目录文件，确保parentId为0
        if (createReqVO.getParentId() == null) {
            file.setParentId(0L);
        }
        fileManageMapper.insert(file);
        return file.getId();
    }

    @Override
    public void updateFile(FileManageSaveReqVO updateReqVO) {
        FileManageDO oldFile = fileManageMapper.selectById(updateReqVO.getId());
        if (oldFile == null) {
            throw new IllegalArgumentException("文件不存在");
        }

        FileManageDO file = BeanUtils.toBean(updateReqVO, FileManageDO.class);
        fileManageMapper.updateById(file);
    }

    @Override
    public void deleteFile(Long id) {
        FileManageDO file = fileManageMapper.selectById(id);
        if (file == null) {
            throw new IllegalArgumentException("文件不存在");
        }

        // 如果是文件夹，检查是否有子文件
        if ("folder".equals(file.getType())) {
            List<FileManageDO> children = fileManageMapper.selectListByParentId(id);
            if (!children.isEmpty()) {
                throw new IllegalArgumentException("该文件夹下存在子文件，无法删除");
            }
        }

        fileManageMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFileList(List<Long> ids) {
        for (Long id : ids) {
            deleteFile(id);
        }
    }

    @Override
    public FileManageDO getFile(Long id) {
        return fileManageMapper.selectById(id);
    }

    @Override
    public List<FileManageDO> getFileList(FileManageListReqVO reqVO) {
        LambdaQueryWrapper<FileManageDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(reqVO.getKeyword())) {
            wrapper.like(FileManageDO::getName, reqVO.getKeyword());
        }
        if (StringUtils.hasText(reqVO.getType())) {
            wrapper.eq(FileManageDO::getType, reqVO.getType());
        }
        if (reqVO.getParentId() != null) {
            wrapper.eq(FileManageDO::getParentId, reqVO.getParentId());
        }
        wrapper.orderByAsc(FileManageDO::getSort);
        return fileManageMapper.selectList(wrapper);
    }

    @Override
    public List<FileManageRespVO> getFileTree() {
        List<FileManageDO> allFiles = fileManageMapper.selectList(new LambdaQueryWrapper<>());

        // 如果没有文件，为当前租户创建默认根目录
        if (allFiles.isEmpty()) {
            initDefaultRootFolder();
            allFiles = fileManageMapper.selectList(new LambdaQueryWrapper<>());
        }

        return buildTree(allFiles, 0L);
    }

    /**
     * 初始化默认根目录
     */
    private void initDefaultRootFolder() {
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("无法获取当前租户ID");
        }

        // 检查是否已经有根目录
        List<FileManageDO> rootFiles = fileManageMapper.selectListRoot();
        if (!rootFiles.isEmpty() && rootFiles.stream().anyMatch(f -> tenantId.equals(f.getTenantId()))) {
            return; // 已经有根目录，无需重复创建
        }

        // 创建默认的"我的项目"根目录
        FileManageDO rootFolder = new FileManageDO();
        rootFolder.setName("我的项目");
        rootFolder.setType("folder");
        rootFolder.setParentId(0L);
        rootFolder.setSort(0);
        rootFolder.setStatus(1);
        rootFolder.setTenantId(tenantId);
        fileManageMapper.insert(rootFolder);
    }

    @Override
    public List<FileManageRespVO> getFilesByParentId(Long parentId) {
        List<FileManageDO> files = fileManageMapper.selectListByParentId(parentId);
        return BeanUtils.toBean(files, FileManageRespVO.class);
    }

    @Override
    public List<FileManageRespVO> searchFiles(String keyword) {
        List<FileManageDO> files = fileManageMapper.selectListByKeyword(keyword);
        return BeanUtils.toBean(files, FileManageRespVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveFile(Long id, Long targetParentId) {
        FileManageDO file = fileManageMapper.selectById(id);
        if (file == null) {
            throw new IllegalArgumentException("文件不存在");
        }

        // 验证源文件属于当前租户
        Long tenantId = TenantContextHolder.getTenantId();
        if (!tenantId.equals(file.getTenantId())) {
            throw new IllegalArgumentException("无权操作该文件");
        }

        // 检查目标父文件夹是否存在
        if (targetParentId > 0) {
            FileManageDO targetParent = fileManageMapper.selectById(targetParentId);
            if (targetParent == null) {
                throw new IllegalArgumentException("目标父文件夹不存在");
            }
            if (!"folder".equals(targetParent.getType())) {
                throw new IllegalArgumentException("目标不是文件夹");
            }
            // 验证目标父文件夹属于当前租户
            if (!tenantId.equals(targetParent.getTenantId())) {
                throw new IllegalArgumentException("无权访问目标父文件夹");
            }
        }

        file.setParentId(targetParentId);
        fileManageMapper.updateById(file);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void renameFile(Long id, String name) {
        FileManageDO file = fileManageMapper.selectById(id);
        if (file == null) {
            throw new IllegalArgumentException("文件不存在");
        }

        // 验证文件属于当前租户
        Long tenantId = TenantContextHolder.getTenantId();
        if (!tenantId.equals(file.getTenantId())) {
            throw new IllegalArgumentException("无权操作该文件");
        }

        file.setName(name);
        fileManageMapper.updateById(file);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveFileContent(Long id, String content) {
        FileManageDO file = fileManageMapper.selectById(id);
        if (file == null) {
            throw new IllegalArgumentException("文件不存在");
        }

        // 验证文件属于当前租户
        Long tenantId = TenantContextHolder.getTenantId();
        if (!tenantId.equals(file.getTenantId())) {
            throw new IllegalArgumentException("无权操作该文件");
        }

        if ("folder".equals(file.getType())) {
            throw new IllegalArgumentException("文件夹没有内容");
        }

        file.setContent(content);
        if (content != null) {
            file.setFileSize((long) content.getBytes().length);
        }
        fileManageMapper.updateById(file);
    }

    @Override
    public String getFileContent(Long id) {
        FileManageDO file = fileManageMapper.selectById(id);
        if (file == null) {
            throw new IllegalArgumentException("文件不存在");
        }

        // 验证文件属于当前租户
        Long tenantId = TenantContextHolder.getTenantId();
        if (!tenantId.equals(file.getTenantId())) {
            throw new IllegalArgumentException("无权访问该文件");
        }

        return file.getContent();
    }

    /**
     * 构建树形结构
     *
     * @param files 所有文件列表
     * @param parentId 父ID
     * @return 树形结构列表
     */
    private List<FileManageRespVO> buildTree(List<FileManageDO> files, Long parentId) {
        List<FileManageRespVO> result = new ArrayList<>();

        for (FileManageDO file : files) {
            if (Objects.equals(file.getParentId(), parentId)) {
                FileManageRespVO respVO = BeanUtils.toBean(file, FileManageRespVO.class);

                // 递归构建子节点
                List<FileManageRespVO> children = buildTree(files, file.getId());
                if (!children.isEmpty()) {
                    respVO.setChildren(children);
                }

                result.add(respVO);
            }
        }

        // 按sort排序
        result.sort(Comparator.comparing(FileManageRespVO::getSort));
        return result;
    }

}
