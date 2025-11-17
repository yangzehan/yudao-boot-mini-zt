package cn.iocoder.yudao.module.datastudio.dal.mysql.file;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.datastudio.controller.admin.file.vo.file.FileManageListReqVO;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.file.FileManageDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * 文件管理 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface FileManageMapper extends BaseMapperX<FileManageDO> {

    /**
     * 根据父ID查询文件列表
     *
     * @param parentId 父ID
     * @return 文件列表
     */
    default List<FileManageDO> selectListByParentId(Long parentId) {
        return selectList(FileManageDO::getParentId, parentId, FileManageDO::getSort, "asc");
    }

    /**
     * 根据父ID集合查询文件列表
     *
     * @param parentIds 父ID集合
     * @return 文件列表
     */
    default List<FileManageDO> selectListByParentIdIn(Collection<Long> parentIds) {
        return selectList(FileManageDO::getParentId, parentIds);
    }

    /**
     * 根据文件类型查询文件列表
     *
     * @param type 文件类型
     * @return 文件列表
     */
    default List<FileManageDO> selectListByType(String type) {
        return selectList(FileManageDO::getType, type);
    }

    /**
     * 根据文件路径查询文件
     *
     * @param filePath 文件路径
     * @return 文件
     */
    default FileManageDO selectByFilePath(String filePath) {
        return selectOne(FileManageDO::getFilePath, filePath);
    }

    /**
     * 搜索文件列表
     *
     * @param keyword 关键词
     * @return 文件列表
     */
    default List<FileManageDO> selectListByKeyword(String keyword) {
        return selectList(new LambdaQueryWrapperX<FileManageDO>()
                .likeIfPresent(FileManageDO::getName, keyword)
                .orderByAsc(FileManageDO::getSort));
    }

    /**
     * 获取根目录文件列表（parentId为0或null的）
     *
     * @return 根目录文件列表
     */
    default List<FileManageDO> selectListRoot() {
        return selectList(new LambdaQueryWrapperX<FileManageDO>()
                .or().isNull(FileManageDO::getParentId)
                .or().eq(FileManageDO::getParentId, 0L)
                .orderByAsc(FileManageDO::getSort));
    }

}
