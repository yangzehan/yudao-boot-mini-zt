package cn.iocoder.yudao.module.datastudio.service.datasource.impl;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.string.StrUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.datastudio.controller.admin.datasource.vo.*;
import cn.iocoder.yudao.module.datastudio.dal.dataobject.datasource.DataSourceDO;
import cn.iocoder.yudao.module.datastudio.dal.mysql.datasource.DataSourceMapper;
import cn.iocoder.yudao.module.datastudio.service.datasource.DataSourceService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.util.CollectionUtils.*;

/**
 * 数据源配置 Service 实现
 *
 * @author 芋道源码
 */
@Slf4j
@Service
public class DataSourceServiceImpl implements DataSourceService {

    @Resource
    private DataSourceMapper dataSourceMapper;

    /**
     * 数据源类型配置
     */
    private static final Map<String, DataSourceTypeRespVO> DATA_SOURCE_TYPES = new HashMap<>();

    static {
        // 初始化数据源类型配置
        DATA_SOURCE_TYPES.put("mysql", new DataSourceTypeRespVO() {{
            setType("mysql");
            setName("MySQL");
            setDriverClassName("com.mysql.cj.jdbc.Driver");
            setDefaultPort(3306);
            setDefaultUrlTemplate("jdbc:mysql://{host}:{port}/{database}?useUnicode=true&characterEncoding=UTF-8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai");
            setDefaultDriver("mysql:mysql-connector-java");
            setSupported(true);
            setIcon("mysql");
        }});

        DATA_SOURCE_TYPES.put("postgresql", new DataSourceTypeRespVO() {{
            setType("postgresql");
            setName("PostgreSQL");
            setDriverClassName("org.postgresql.Driver");
            setDefaultPort(5432);
            setDefaultUrlTemplate("jdbc:postgresql://{host}:{port}/{database}");
            setDefaultDriver("org.postgresql:postgresql");
            setSupported(true);
            setIcon("postgresql");
        }});

        DATA_SOURCE_TYPES.put("oracle", new DataSourceTypeRespVO() {{
            setType("oracle");
            setName("Oracle");
            setDriverClassName("oracle.jdbc.driver.OracleDriver");
            setDefaultPort(1521);
            setDefaultUrlTemplate("jdbc:oracle:thin:@{host}:{port}:{database}");
            setDefaultDriver("com.oracle.database.jdbc:ojdbc8");
            setSupported(true);
            setIcon("oracle");
        }});

        DATA_SOURCE_TYPES.put("sqlserver", new DataSourceTypeRespVO() {{
            setType("sqlserver");
            setName("SQL Server");
            setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            setDefaultPort(1433);
            setDefaultUrlTemplate("jdbc:sqlserver://{host}:{port};DatabaseName={database}");
            setDefaultDriver("com.microsoft.sqlserver:mssql-jdbc");
            setSupported(true);
            setIcon("sqlserver");
        }});

        DATA_SOURCE_TYPES.put("clickhouse", new DataSourceTypeRespVO() {{
            setType("clickhouse");
            setName("ClickHouse");
            setDriverClassName("com.clickhouse.jdbc.ClickHouseDriver");
            setDefaultPort(8123);
            setDefaultUrlTemplate("jdbc:clickhouse://{host}:{port}/{database}");
            setDefaultDriver("com.clickhouse:clickhouse-jdbc");
            setSupported(true);
            setIcon("clickhouse");
        }});

        DATA_SOURCE_TYPES.put("hive", new DataSourceTypeRespVO() {{
            setType("hive");
            setName("Hive");
            setDriverClassName("org.apache.hive.jdbc.HiveDriver");
            setDefaultPort(10000);
            setDefaultUrlTemplate("jdbc:hive2://{host}:{port}/{database}");
            setDefaultDriver("org.apache.hive:hive-jdbc");
            setSupported(true);
            setIcon("hive");
        }});
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDataSource(@Valid DataSourceSaveReqVO createReqVO) {
        // 1. 校验名称唯一性
        DataSourceDO existDataSource = dataSourceMapper.selectByName(createReqVO.getName());
        if (existDataSource != null) {
            throw new IllegalArgumentException("数据源名称已存在：" + createReqVO.getName());
        }

        // 2. 构建数据源配置
        DataSourceDO dataSource = BeanUtils.toBean(createReqVO, DataSourceDO.class);

        // 3. 自动填充驱动类和URL
        autoFillDriverAndUrl(dataSource);

        // 4. 保存数据源
        dataSourceMapper.insert(dataSource);

        return dataSource.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDataSource(@Valid DataSourceSaveReqVO updateReqVO) {
        // 1. 校验存在
        DataSourceDO existDataSource = dataSourceMapper.selectById(updateReqVO.getId());
        if (existDataSource == null) {
            throw new IllegalArgumentException("数据源不存在：" + updateReqVO.getId());
        }

        // 2. 校验名称唯一性（排除当前记录）
        DataSourceDO duplicateDataSource = dataSourceMapper.selectByName(updateReqVO.getName());
        if (duplicateDataSource != null && !duplicateDataSource.getId().equals(updateReqVO.getId())) {
            throw new IllegalArgumentException("数据源名称已存在：" + updateReqVO.getName());
        }

        // 3. 更新数据源
        DataSourceDO dataSource = BeanUtils.toBean(updateReqVO, DataSourceDO.class);
        autoFillDriverAndUrl(dataSource);
        dataSource.setUpdateTime(LocalDateTime.now());
        dataSourceMapper.updateById(dataSource);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDataSource(Long id) {
        dataSourceMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDataSourceList(List<Long> ids) {
        if (isEmpty(ids)) {
            return;
        };
        dataSourceMapper.deleteBatchIds(ids);
    }

    @Override
    public DataSourceDO getDataSource(Long id) {
        return dataSourceMapper.selectById(id);
    }

    @Override
    public List<DataSourceDO> getDataSourceList(String name, String type, Integer status) {
        return dataSourceMapper.selectList(name, type, status);
    }

    @Override
    public PageResult<DataSourceDO> getDataSourcePage(DataSourcePageReqVO pageReqVO) {
        return dataSourceMapper.selectPage(pageReqVO);
    }

    @Override
    public Boolean testConnection(Long id) {
        DataSourceDO dataSource = dataSourceMapper.selectById(id);
        if (dataSource == null) {
            throw new IllegalArgumentException("数据源不存在：" + id);
        }

        DataSourceSaveReqVO reqVO = BeanUtils.toBean(dataSource, DataSourceSaveReqVO.class);
        Boolean result = testConnection(reqVO);

        // 更新连接状态和测试时间
        dataSource.setConnectionStatus(result ? "connected" : "error");
        dataSource.setLastConnectionTime(System.currentTimeMillis());
        dataSourceMapper.updateById(dataSource);

        return result;
    }

    @Override
    public Boolean testConnection(DataSourceSaveReqVO saveReqVO) {
        DataSourceTypeRespVO typeConfig = DATA_SOURCE_TYPES.get(saveReqVO.getType());
        if (typeConfig == null) {
            throw new IllegalArgumentException("不支持的数据源类型：" + saveReqVO.getType());
        }

        try {
            // 1. 加载驱动
            Class.forName(typeConfig.getDriverClassName());

            // 2. 构建连接URL
            String url = saveReqVO.getUrl();
            if (!StringUtils.hasText(url)) {
                url = buildUrl(saveReqVO, typeConfig.getDefaultUrlTemplate());
            }

            // 3. 测试连接
            long startTime = System.currentTimeMillis();
            try (Connection conn = DriverManager.getConnection(url, saveReqVO.getUsername(), saveReqVO.getPassword())) {
                long responseTime = System.currentTimeMillis() - startTime;
                log.info("数据源连接测试成功：{}，耗时：{}ms", saveReqVO.getName(), responseTime);
                return true;
            }
        } catch (ClassNotFoundException e) {
            log.error("数据源驱动加载失败：{}", saveReqVO.getType(), e);
            throw new IllegalArgumentException("数据源驱动不存在：" + saveReqVO.getType());
        } catch (SQLException e) {
            log.error("数据源连接测试失败：{}", saveReqVO.getName(), e);
            throw new IllegalArgumentException("连接失败：" + e.getMessage());
        }
    }

    @Override
    public List<DataSourceTypeRespVO> getDataSourceTypes() {
        return new ArrayList<>(DATA_SOURCE_TYPES.values());
    }

    /**
     * 自动填充驱动类和URL
     */
    private void autoFillDriverAndUrl(DataSourceDO dataSource) {
        DataSourceTypeRespVO typeConfig = DATA_SOURCE_TYPES.get(dataSource.getType());
        if (typeConfig == null) {
            throw new IllegalArgumentException("不支持的数据源类型：" + dataSource.getType());
        }

        // 填充驱动类名
        if (!StringUtils.hasText(dataSource.getDriverClassName())) {
            dataSource.setDriverClassName(typeConfig.getDriverClassName());
        }

        // 填充URL
        if (!StringUtils.hasText(dataSource.getUrl())) {
            dataSource.setUrl(buildUrl(dataSource, typeConfig.getDefaultUrlTemplate()));
        }
    }

    /**
     * 构建连接URL
     */
    private String buildUrl(Object obj, String template) {
        String url = template;
        if (obj instanceof DataSourceDO) {
            DataSourceDO dataSource = (DataSourceDO) obj;
            url = url.replace("{host}", dataSource.getHost() != null ? dataSource.getHost() : "")
                     .replace("{port}", dataSource.getPort() != null ? String.valueOf(dataSource.getPort()) : "")
                     .replace("{database}", dataSource.getDatabase() != null ? dataSource.getDatabase() : "");
        } else if (obj instanceof DataSourceSaveReqVO) {
            DataSourceSaveReqVO reqVO = (DataSourceSaveReqVO) obj;
            url = url.replace("{host}", reqVO.getHost() != null ? reqVO.getHost() : "")
                     .replace("{port}", reqVO.getPort() != null ? String.valueOf(reqVO.getPort()) : "")
                     .replace("{database}", reqVO.getDatabase() != null ? reqVO.getDatabase() : "");
        }
        return url;
    }

    @Override
    public List<String> getTables(Long id) {
        DataSourceDO dataSource = dataSourceMapper.selectById(id);
        if (dataSource == null) {
            throw new IllegalArgumentException("数据源不存在：" + id);
        }

        List<String> tables = new ArrayList<>();
        Connection conn = null;
        ResultSet rs = null;

        try {
            // 测试连接并获取表列表
            DataSourceTypeRespVO typeConfig = DATA_SOURCE_TYPES.get(dataSource.getType());
            if (typeConfig == null) {
                throw new IllegalArgumentException("不支持的数据源类型：" + dataSource.getType());
            }

            // 加载驱动
            Class.forName(typeConfig.getDriverClassName());

            // 获取连接
            conn = DriverManager.getConnection(
                    dataSource.getUrl(),
                    dataSource.getUsername(),
                    dataSource.getPassword()
            );

            // 获取数据库元数据
            DatabaseMetaData metaData = conn.getMetaData();
            String catalog = null;
            String schemaPattern = null;

            // 对于不同数据库，需要使用不同的catalog和schema
            // MySQL使用database作为catalog
            if ("mysql".equals(dataSource.getType())) {
                catalog = dataSource.getDatabase();
            }

            rs = metaData.getTables(catalog, schemaPattern, "%", new String[]{"TABLE"});

            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                if (tableName != null && !tableName.isEmpty()) {
                    tables.add(tableName);
                }
            }

            log.info("获取数据源表列表成功：{}，共 {} 个表", dataSource.getName(), tables.size());
            return tables;

        } catch (ClassNotFoundException e) {
            log.error("数据源驱动加载失败：{}", dataSource.getType(), e);
            throw new IllegalArgumentException("数据源驱动不存在：" + dataSource.getType());
        } catch (SQLException e) {
            log.error("获取数据源表列表失败：{}", dataSource.getName(), e);
            throw new IllegalArgumentException("获取表列表失败：" + e.getMessage());
        } finally {
            // 关闭资源
            try {
                if (rs != null) rs.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                log.warn("关闭数据库连接失败", e);
            }
        }
    }

}
