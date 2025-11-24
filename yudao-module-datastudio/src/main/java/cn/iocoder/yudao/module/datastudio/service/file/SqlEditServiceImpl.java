package cn.iocoder.yudao.module.datastudio.service.file;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.datastudio.controller.admin.file.vo.file.SqlEditListReqVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.file.vo.resp.SqlEditDataRespVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.file.vo.resp.SqlEditRespVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.file.vo.save.SqlEditDataSaveReqVO;
import cn.iocoder.yudao.module.datastudio.controller.admin.file.vo.save.SqlEditSaveReqVO;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.file.SqlEditConfigDO;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.file.SqlEditDO;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.file.SqlEditVersionDO;
import cn.iocoder.yudao.module.datastudio.dal.mysql.file.SqlEditConfigMapper;
import cn.iocoder.yudao.module.datastudio.dal.mysql.file.SqlEditMapper;
import cn.iocoder.yudao.module.datastudio.dal.mysql.file.SqlEditVersionMapper;
import cn.iocoder.yudao.module.datastudio.dto.flink.FlinkConfig;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * SQL 编辑器 Service 实现
 *
 * @author 芋道源码
 */
@Service
public class SqlEditServiceImpl implements SqlEditService {

    @Resource
    private SqlEditMapper sqlEditMapper;

    @Resource
    private SqlEditConfigMapper sqlEditConfigMapper;

    @Resource
    private SqlEditVersionMapper sqlEditVersionMapper;

    @Resource
    private SqlEditVersionService sqlEditVersionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createFile(SqlEditSaveReqVO createReqVO) {
        // 获取当前租户ID
        Long tenantId = TenantContextHolder.getTenantId();

        // 验证父文件夹是否存在
        if (createReqVO.getParentId() != null && createReqVO.getParentId() > 0) {
            SqlEditDO parent = sqlEditMapper.selectById(createReqVO.getParentId());
            if (parent == null) {
                throw new IllegalArgumentException("父文件夹不存在");
            }
            // 验证父文件夹属于当前租户
            if (!tenantId.equals(parent.getTenantId())) {
                throw new IllegalArgumentException("无权访问该父文件夹");
            }
        }

        SqlEditDO file = BeanUtils.toBean(createReqVO, SqlEditDO.class);
        // 确保设置租户ID
        if (file.getTenantId() == null) {
            file.setTenantId(tenantId);
        }
        // 如果是创建根目录文件，确保parentId为0
        if (createReqVO.getParentId() == null) {
            file.setParentId(0L);
        }
        sqlEditMapper.insert(file);
        return file.getId();
    }

    @Override
    public void updateFile(SqlEditSaveReqVO updateReqVO) {
        SqlEditDO oldFile = sqlEditMapper.selectById(updateReqVO.getId());
        if (oldFile == null) {
            throw new IllegalArgumentException("文件不存在");
        }

        SqlEditDO file = BeanUtils.toBean(updateReqVO, SqlEditDO.class);
        sqlEditMapper.updateById(file);
    }

    @Override
    public void deleteFile(Long id) {
        SqlEditDO file = sqlEditMapper.selectById(id);
        if (file == null) {
            throw new IllegalArgumentException("文件不存在");
        }

        // 如果是文件夹，检查是否有子文件
        if ("folder".equals(file.getType())) {
            List<SqlEditDO> children = sqlEditMapper.selectListByParentId(id);
            if (!children.isEmpty()) {
                throw new IllegalArgumentException("该文件夹下存在子文件，无法删除");
            }
        }

        sqlEditMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFileList(List<Long> ids) {
        for (Long id : ids) {
            deleteFile(id);
        }
    }

    @Override
    public SqlEditDO getFile(Long id) {
        return sqlEditMapper.selectById(id);
    }

    @Override
    public List<SqlEditDO> getFileList(SqlEditListReqVO reqVO) {
        LambdaQueryWrapper<SqlEditDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(reqVO.getKeyword())) {
            wrapper.like(SqlEditDO::getName, reqVO.getKeyword());
        }
        if (StringUtils.hasText(reqVO.getType())) {
            wrapper.eq(SqlEditDO::getType, reqVO.getType());
        }
        if (reqVO.getParentId() != null) {
            wrapper.eq(SqlEditDO::getParentId, reqVO.getParentId());
        }
        wrapper.orderByAsc(SqlEditDO::getSort);
        return sqlEditMapper.selectList(wrapper);
    }

    @Override
    public List<SqlEditRespVO> getFileTree() {
        List<SqlEditDO> allFiles = sqlEditMapper.selectList(new LambdaQueryWrapper<>());

        // 如果没有文件，为当前租户创建默认根目录
        if (allFiles.isEmpty()) {
            initDefaultRootFolder();
            allFiles = sqlEditMapper.selectList(new LambdaQueryWrapper<>());
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
        List<SqlEditDO> rootFiles = sqlEditMapper.selectListRoot();
        if (!rootFiles.isEmpty() && rootFiles.stream().anyMatch(f -> tenantId.equals(f.getTenantId()))) {
            return; // 已经有根目录，无需重复创建
        }

        // 创建默认的"我的项目"根目录
        SqlEditDO rootFolder = new SqlEditDO();
        rootFolder.setName("我的项目");
        rootFolder.setType("folder");
        rootFolder.setParentId(0L);
        rootFolder.setSort(0);
        rootFolder.setStatus(1);
        rootFolder.setTenantId(tenantId);
        sqlEditMapper.insert(rootFolder);
    }

    @Override
    public List<SqlEditRespVO> getFilesByParentId(Long parentId) {
        List<SqlEditDO> files = sqlEditMapper.selectListByParentId(parentId);
        return BeanUtils.toBean(files, SqlEditRespVO.class);
    }

    @Override
    public List<SqlEditRespVO> searchFiles(String keyword) {
        List<SqlEditDO> files = sqlEditMapper.selectListByKeyword(keyword);
        return BeanUtils.toBean(files, SqlEditRespVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveFile(Long id, Long targetParentId) {
        SqlEditDO file = sqlEditMapper.selectById(id);
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
            SqlEditDO targetParent = sqlEditMapper.selectById(targetParentId);
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
        sqlEditMapper.updateById(file);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void renameFile(Long id, String name) {
        SqlEditDO file = sqlEditMapper.selectById(id);
        if (file == null) {
            throw new IllegalArgumentException("文件不存在");
        }

        // 验证文件属于当前租户
        Long tenantId = TenantContextHolder.getTenantId();
        if (!tenantId.equals(file.getTenantId())) {
            throw new IllegalArgumentException("无权操作该文件");
        }

        file.setName(name);
        sqlEditMapper.updateById(file);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveFileContent(Long id, String content) {
        SqlEditDO file = sqlEditMapper.selectById(id);
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
        sqlEditMapper.updateById(file);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveFileData(SqlEditSaveReqVO saveReqVO) {
        SqlEditDO file = sqlEditMapper.selectById(saveReqVO.getId());
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

        // 保存文件内容
        if (saveReqVO.getContent() != null) {
            file.setContent(saveReqVO.getContent());
            file.setFileSize((long) saveReqVO.getContent().getBytes().length);
        }

        sqlEditMapper.updateById(file);
    }

    @Override
    public String getFileContent(Long id) {
        SqlEditDO file = sqlEditMapper.selectById(id);
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

    @Override
    public SqlEditDataRespVO getFileData(Long id) {
        SqlEditDO file = sqlEditMapper.selectById(id);
        if (file == null) {
            throw new IllegalArgumentException("文件不存在");
        }

        // 验证文件属于当前租户
        Long tenantId = TenantContextHolder.getTenantId();
        if (!tenantId.equals(file.getTenantId())) {
            throw new IllegalArgumentException("无权访问该文件");
        }

        // 先转换基本信息
        SqlEditDataRespVO dataRespVO = BeanUtils.toBean(file, SqlEditDataRespVO.class);

        // 获取配置信息
        FlinkConfig config = getFileConfig(id);
        dataRespVO.setConfig(config);

        return dataRespVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveFileData(SqlEditDataSaveReqVO saveReqVO) {
        SqlEditDO file = sqlEditMapper.selectById(saveReqVO.getId());
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

        // 检查内容是否有变化
        String oldContent = file.getContent();
        String newContent = saveReqVO.getContent();

        // 保存文件内容
        if (saveReqVO.getContent() != null) {
            file.setContent(saveReqVO.getContent());
            file.setFileSize((long) saveReqVO.getContent().getBytes().length);
        }
        sqlEditMapper.updateById(file);

        // 保存配置信息
        if (saveReqVO.getConfig() != null) {
            saveFileConfig(saveReqVO.getId(), saveReqVO.getConfig());
        } else {
            // 如果配置为空，删除配置记录
            deleteFileConfig(saveReqVO.getId());
        }


            // 获取当前文件配置
            FlinkConfig currentConfig = saveReqVO.getConfig();
            if (currentConfig == null) {
                currentConfig = getFileConfig(saveReqVO.getId());
            }

            // 创建版本记录
            SqlEditVersionDO version = new SqlEditVersionDO();
            version.setSqlEditId(saveReqVO.getId());
            version.setContent(newContent);
            version.setConfig(currentConfig);
            version.setVersionType("auto");
            version.setRemark("自动保存版本");

            // 获取当前最新版本号并递增
            Long currentVersionNumber = sqlEditVersionMapper.selectMaxVersionNumberBySqlEditId(saveReqVO.getId());
            version.setVersionNumber(currentVersionNumber + 1);

            // 保存版本
            sqlEditVersionMapper.insert(version);

            // 检查版本数量，如果超过7个则删除最旧的版本
            sqlEditVersionService.deleteOldVersions(saveReqVO.getId(), 7);

    }

    @Override
    public FlinkConfig getFileConfig(Long sqlEditId) {

        SqlEditConfigDO configDO = sqlEditConfigMapper.selectBySqlEditId(sqlEditId);
        if (configDO == null || Objects.isNull(configDO.getConfig())) {
            return null;
        }
        return configDO.getConfig();

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveFileConfig(Long sqlEditId, FlinkConfig config) {
        // 先删除旧配置
        sqlEditConfigMapper.deleteBySqlEditId(sqlEditId);

        // 保存新配置
        SqlEditConfigDO configDO = new SqlEditConfigDO();
        configDO.setSqlEditId(sqlEditId);
        configDO.setConfig(config);
        sqlEditConfigMapper.insert(configDO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFileConfig(Long sqlEditId) {
        sqlEditConfigMapper.deleteBySqlEditId(sqlEditId);
    }

    /**
     * 构建树形结构
     *
     * @param files 所有文件列表
     * @param parentId 父ID
     * @return 树形结构列表
     */
    private List<SqlEditRespVO> buildTree(List<SqlEditDO> files, Long parentId) {
        List<SqlEditRespVO> result = new ArrayList<>();

        for (SqlEditDO file : files) {
            if (Objects.equals(file.getParentId(), parentId)) {
                SqlEditRespVO respVO = BeanUtils.toBean(file, SqlEditRespVO.class);

                // 递归构建子节点
                List<SqlEditRespVO> children = buildTree(files, file.getId());
                if (!children.isEmpty()) {
                    respVO.setChildren(children);
                }

                result.add(respVO);
            }
        }

        // 按sort排序
        result.sort(Comparator.comparing(SqlEditRespVO::getSort));
        return result;
    }

}
