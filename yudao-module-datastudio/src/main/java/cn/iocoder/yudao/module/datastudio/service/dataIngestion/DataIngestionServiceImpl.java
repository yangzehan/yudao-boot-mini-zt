package cn.iocoder.yudao.module.datastudio.service.dataIngestion;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.datastudio.controller.admin.dataIngestion.vo.file.DataIngestionListReqVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.dataIngestion.vo.resp.DataIngestionRespVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.dataIngestion.vo.save.DataIngestionSaveReqVO;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.dataIngestion.DataIngestionDO;
import cn.iocoder.yudao.module.datastudio.dal.mysql.dataIngestion.DataIngestionMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;

/**
 * 数据摄取 Service 实现
 *
 * @author 芋道源码
 */
@Service
public class DataIngestionServiceImpl implements DataIngestionService {

    @Resource
    private DataIngestionMapper dataIngestionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createFile(DataIngestionSaveReqVO createReqVO) {
        // 获取当前租户ID
        Long tenantId = TenantContextHolder.getTenantId();

        // 验证父文件夹是否存在
        if (createReqVO.getParentId() != null && createReqVO.getParentId() > 0) {
            DataIngestionDO parent = dataIngestionMapper.selectById(createReqVO.getParentId());
            if (parent == null) {
                throw new IllegalArgumentException("父文件夹不存在");
            }
            // 验证父文件夹属于当前租户
            if (!tenantId.equals(parent.getTenantId())) {
                throw new IllegalArgumentException("无权访问该父文件夹");
            }
        }

        DataIngestionDO file = BeanUtils.toBean(createReqVO, DataIngestionDO.class);
        // 确保设置租户ID
        if (file.getTenantId() == null) {
            file.setTenantId(tenantId);
        }
        // 如果是创建根目录文件，确保parentId为0
        if (createReqVO.getParentId() == null) {
            file.setParentId(0L);
        }
        dataIngestionMapper.insert(file);
        return file.getId();
    }

    @Override
    public void updateFile(DataIngestionSaveReqVO updateReqVO) {
        DataIngestionDO oldFile = dataIngestionMapper.selectById(updateReqVO.getId());
        if (oldFile == null) {
            throw new IllegalArgumentException("文件不存在");
        }

        DataIngestionDO file = BeanUtils.toBean(updateReqVO, DataIngestionDO.class);
        dataIngestionMapper.updateById(file);
    }

    @Override
    public void deleteFile(Long id) {
        DataIngestionDO file = dataIngestionMapper.selectById(id);
        if (file == null) {
            throw new IllegalArgumentException("文件不存在");
        }

        // 如果是文件夹，检查是否有子文件
        if ("folder".equals(file.getType())) {
            List<DataIngestionDO> children = dataIngestionMapper.selectListByParentId(id);
            if (!children.isEmpty()) {
                throw new IllegalArgumentException("该文件夹下存在子文件，无法删除");
            }
        }

        dataIngestionMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFileList(List<Long> ids) {
        for (Long id : ids) {
            deleteFile(id);
        }
    }

    @Override
    public DataIngestionDO getFile(Long id) {
        return dataIngestionMapper.selectById(id);
    }

    @Override
    public List<DataIngestionDO> getFileList(DataIngestionListReqVO reqVO) {
        LambdaQueryWrapper<DataIngestionDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(reqVO.getKeyword())) {
            wrapper.like(DataIngestionDO::getName, reqVO.getKeyword());
        }
        if (StringUtils.hasText(reqVO.getType())) {
            wrapper.eq(DataIngestionDO::getType, reqVO.getType());
        }
        if (reqVO.getParentId() != null) {
            wrapper.eq(DataIngestionDO::getParentId, reqVO.getParentId());
        }
        wrapper.orderByAsc(DataIngestionDO::getSort);
        return dataIngestionMapper.selectList(wrapper);
    }

    @Override
    public List<DataIngestionRespVO> getFileTree() {
        List<DataIngestionDO> allFiles = dataIngestionMapper.selectList(new LambdaQueryWrapper<>());

        // 如果没有文件，为当前租户创建默认根目录
        if (allFiles.isEmpty()) {
            initDefaultRootFolder();
            allFiles = dataIngestionMapper.selectList(new LambdaQueryWrapper<>());
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
        List<DataIngestionDO> rootFiles = dataIngestionMapper.selectListRoot();
        if (!rootFiles.isEmpty() && rootFiles.stream().anyMatch(f -> tenantId.equals(f.getTenantId()))) {
            return; // 已经有根目录，无需重复创建
        }

        // 创建默认的"数据摄取"根目录
        DataIngestionDO rootFolder = new DataIngestionDO();
        rootFolder.setName("数据摄取");
        rootFolder.setType("folder");
        rootFolder.setParentId(0L);
        rootFolder.setSort(0);
        rootFolder.setStatus(1);
        rootFolder.setTenantId(tenantId);
        dataIngestionMapper.insert(rootFolder);
    }

    @Override
    public List<DataIngestionRespVO> getFilesByParentId(Long parentId) {
        List<DataIngestionDO> files = dataIngestionMapper.selectListByParentId(parentId);
        return BeanUtils.toBean(files, DataIngestionRespVO.class);
    }

    @Override
    public List<DataIngestionRespVO> searchFiles(String keyword) {
        List<DataIngestionDO> files = dataIngestionMapper.selectListByKeyword(keyword);
        return BeanUtils.toBean(files, DataIngestionRespVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveFile(Long id, Long targetParentId) {
        DataIngestionDO file = dataIngestionMapper.selectById(id);
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
            DataIngestionDO targetParent = dataIngestionMapper.selectById(targetParentId);
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
        dataIngestionMapper.updateById(file);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void renameFile(Long id, String name) {
        DataIngestionDO file = dataIngestionMapper.selectById(id);
        if (file == null) {
            throw new IllegalArgumentException("文件不存在");
        }

        // 验证文件属于当前租户
        Long tenantId = TenantContextHolder.getTenantId();
        if (!tenantId.equals(file.getTenantId())) {
            throw new IllegalArgumentException("无权操作该文件");
        }

        file.setName(name);
        dataIngestionMapper.updateById(file);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveFileContent(Long id, String content) {
        DataIngestionDO file = dataIngestionMapper.selectById(id);
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
        dataIngestionMapper.updateById(file);
    }

    @Override
    public String getFileContent(Long id) {
        DataIngestionDO file = dataIngestionMapper.selectById(id);
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
    private List<DataIngestionRespVO> buildTree(List<DataIngestionDO> files, Long parentId) {
        List<DataIngestionRespVO> result = new ArrayList<>();

        for (DataIngestionDO file : files) {
            if (Objects.equals(file.getParentId(), parentId)) {
                DataIngestionRespVO respVO = BeanUtils.toBean(file, DataIngestionRespVO.class);

                // 递归构建子节点
                List<DataIngestionRespVO> children = buildTree(files, file.getId());
                if (!children.isEmpty()) {
                    respVO.setChildren(children);
                }

                result.add(respVO);
            }
        }

        // 按sort排序
        result.sort(Comparator.comparing(DataIngestionRespVO::getSort));
        return result;
    }

}
