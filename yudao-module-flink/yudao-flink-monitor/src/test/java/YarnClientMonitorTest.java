import cn.iocoder.yudao.module.datastudio.api.enums.JobStatus;
import cn.iocoder.yudao.module.datastudio.api.job.dto.DataJobDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * YarnClientMonitor 单元测试
 *
 * <p>测试验证逻辑和状态映射，不涉及真实的 YARN 客户端
 *
 * @author yzh
 */
@ExtendWith(MockitoExtension.class)
public class YarnClientMonitorTest {

    private final TestableYarnClientMonitor yarnClientMonitor = new TestableYarnClientMonitor();

    @Test
    @DisplayName("测试 job 为空应抛出异常")
    void testNullJob() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> yarnClientMonitor.getJobStatus(null)
        );

        assertEquals("job 不能为空", exception.getMessage());
    }

    @Test
    @DisplayName("测试非 YARN Application 模式应抛出异常 - session 模式")
    void testNonYarnApplicationMode_Session() {
        DataJobDto job = createNonYarnApplicationJob("session");

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> yarnClientMonitor.getJobStatus(job)
        );

        assertTrue(exception.getMessage().contains("非 YARN Application 模式"));
    }

    @Test
    @DisplayName("测试非 YARN Application 模式应抛出异常 - per-job 模式")
    void testNonYarnApplicationMode_PerJob() {
        DataJobDto job = createNonYarnApplicationJob("per-job");

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> yarnClientMonitor.getJobStatus(job)
        );

        assertTrue(exception.getMessage().contains("非 YARN Application 模式"));
    }

    @Test
    @DisplayName("测试 flinkClusterId 为空应抛出异常")
    void testEmptyFlinkClusterId() {
        DataJobDto job = createYarnApplicationJob();
        job.setFlinkClusterId(null);

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> yarnClientMonitor.getJobStatus(job)
        );

        assertTrue(exception.getMessage().contains("flinkClusterId 为空"));
    }

    @Test
    @DisplayName("测试 flinkClusterId 为空字符串应抛出异常")
    void testEmptyFlinkClusterIdString() {
        DataJobDto job = createYarnApplicationJob();
        job.setFlinkClusterId("");

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> yarnClientMonitor.getJobStatus(job)
        );

        assertTrue(exception.getMessage().contains("flinkClusterId 为空"));
    }

    @Test
    @DisplayName("测试 YARN 配置路径不全 - yarnSitePath 为空")
    void testIncompleteYarnConfigPaths_YarnSitePathNull() {
        DataJobDto job = createYarnApplicationJob();
        job.setYarnSitePath(null);

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> yarnClientMonitor.getJobStatus(job)
        );

        assertTrue(exception.getMessage().contains("YARN 配置路径不齐全"));
    }

    @Test
    @DisplayName("测试 YARN 配置路径不全 - hdfsSitePath 为空")
    void testIncompleteYarnConfigPaths_HdfsSitePathNull() {
        DataJobDto job = createYarnApplicationJob();
        job.setHdfsSitePath(null);

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> yarnClientMonitor.getJobStatus(job)
        );

        assertTrue(exception.getMessage().contains("YARN 配置路径不齐全"));
    }

    @Test
    @DisplayName("测试 YARN 配置路径不全 - coreSitePath 为空")
    void testIncompleteYarnConfigPaths_CoreSitePathNull() {
        DataJobDto job = createYarnApplicationJob();
        job.setCoreSitePath(null);

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> yarnClientMonitor.getJobStatus(job)
        );

        assertTrue(exception.getMessage().contains("YARN 配置路径不齐全"));
    }

    @Test
    @DisplayName("测试 YARN 配置路径为空字符串")
    void testEmptyYarnConfigPaths() {
        DataJobDto job = createYarnApplicationJob();
        job.setYarnSitePath("");

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> yarnClientMonitor.getJobStatus(job)
        );

        assertTrue(exception.getMessage().contains("YARN 配置路径不齐全"));
    }

    @Test
    @DisplayName("测试通过 config.deployMode 识别 YARN Application 模式")
    void testYarnModeDetectionByConfig() {
        DataJobDto job = createYarnApplicationJob();

        boolean result = yarnClientMonitor.testIsYarnApplicationMode(job);
        assertTrue(result, "deployMode=application 应识别为 YARN Application 模式");
    }

    @Test
    @DisplayName("测试通过 flinkClusterId 格式识别 YARN Application 模式")
    void testYarnModeDetectionByClusterId() {
        DataJobDto job = new DataJobDto();
        job.setConfig(new HashMap<>());
        job.setJobId("test-job-id");
        job.setWebUiUrl("http://localhost:8081");
        job.setFlinkClusterId("application_1234567890123_0001");
        job.setYarnSitePath("/path/to/yarn-site.xml");
        job.setHdfsSitePath("/path/to/hdfs-site.xml");
        job.setCoreSitePath("/path/to/core-site.xml");

        boolean result = yarnClientMonitor.testIsYarnApplicationMode(job);
        assertTrue(result, "flinkClusterId 以 application_ 开头应识别为 YARN Application 模式");
    }

    @Test
    @DisplayName("测试 config 中 deployMode 为大写 APPLICATION")
    void testYarnModeDetectionByConfigUpperCase() {
        DataJobDto job = createYarnApplicationJob();
        job.getConfig().put("deployMode", "APPLICATION");

        boolean result = yarnClientMonitor.testIsYarnApplicationMode(job);
        assertTrue(result, "deployMode=APPLICATION 应识别为 YARN Application 模式");
    }

    @Test
    @DisplayName("测试 YARN RUNNING 状态映射")
    void testYarnRunningStateMapping() {
        JobStatus status = yarnClientMonitor.testMapYarnStateToJobStatus(
            org.apache.hadoop.yarn.api.records.YarnApplicationState.RUNNING);
        assertEquals(JobStatus.RUNNING, status);
    }

    @Test
    @DisplayName("测试 YARN FINISHED 状态映射")
    void testYarnFinishedStateMapping() {
        JobStatus status = yarnClientMonitor.testMapYarnStateToJobStatus(
            org.apache.hadoop.yarn.api.records.YarnApplicationState.FINISHED);
        assertEquals(JobStatus.FINISHED, status);
    }

    @Test
    @DisplayName("测试 YARN FAILED 状态映射")
    void testYarnFailedStateMapping() {
        JobStatus status = yarnClientMonitor.testMapYarnStateToJobStatus(
            org.apache.hadoop.yarn.api.records.YarnApplicationState.FAILED);
        assertEquals(JobStatus.FAILED, status);
    }

    @Test
    @DisplayName("测试 YARN KILLED 状态映射")
    void testYarnKilledStateMapping() {
        JobStatus status = yarnClientMonitor.testMapYarnStateToJobStatus(
            org.apache.hadoop.yarn.api.records.YarnApplicationState.KILLED);
        assertEquals(JobStatus.CANCELED, status);
    }

    @Test
    @DisplayName("测试 YARN ACCEPTED 状态映射")
    void testYarnAcceptedStateMapping() {
        JobStatus status = yarnClientMonitor.testMapYarnStateToJobStatus(
            org.apache.hadoop.yarn.api.records.YarnApplicationState.ACCEPTED);
        assertEquals(JobStatus.INITIALIZING, status);
    }

    @Test
    @DisplayName("测试 YARN SUBMITTED 状态映射")
    void testYarnSubmittedStateMapping() {
        JobStatus status = yarnClientMonitor.testMapYarnStateToJobStatus(
            org.apache.hadoop.yarn.api.records.YarnApplicationState.SUBMITTED);
        assertEquals(JobStatus.CREATED, status);
    }

    @Test
    @DisplayName("测试 YARN 未知状态映射")
    void testYarnUnknownStateMapping() {
        JobStatus status = yarnClientMonitor.testMapYarnStateToJobStatus(
            org.apache.hadoop.yarn.api.records.YarnApplicationState.NEW);
        assertEquals(JobStatus.UNKNOWN, status);
    }

    /**
     * 可测试版本的 YarnClientMonitor，将受保护方法暴露给测试
     */
    static class TestableYarnClientMonitor extends cn.iocoder.yudao.module.flink.monitor.monitor.YarnClientMonitor {

        public boolean testIsYarnApplicationMode(DataJobDto job) {
            return isYarnApplicationMode(job);
        }

        public JobStatus testMapYarnStateToJobStatus(org.apache.hadoop.yarn.api.records.YarnApplicationState yarnState) {
            return mapYarnStateToJobStatus(yarnState);
        }
    }

    /**
     * 创建 YARN Application 模式的测试作业
     */
    private DataJobDto createYarnApplicationJob() {
        Map<String, String> config = new HashMap<>();
        config.put("deployMode", "application");

        DataJobDto job = new DataJobDto();
        job.setConfig(config);
        job.setJobId("test-job-id");
        job.setWebUiUrl("http://localhost:8081");
        job.setFlinkClusterId("application_1234567890123_0001");
        job.setYarnSitePath("/path/to/yarn-site.xml");
        job.setHdfsSitePath("/path/to/hdfs-site.xml");
        job.setCoreSitePath("/path/to/core-site.xml");
        return job;
    }

    /**
     * 创建非 YARN Application 模式的测试作业
     */
    private DataJobDto createNonYarnApplicationJob(String deployMode) {
        Map<String, String> config = new HashMap<>();
        config.put("deployMode", deployMode);

        DataJobDto job = new DataJobDto();
        job.setConfig(config);
        job.setJobId("test-job-id");
        job.setWebUiUrl("http://localhost:8081");
        job.setFlinkClusterId(null);
        return job;
    }
}
