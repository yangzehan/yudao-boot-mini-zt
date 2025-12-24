# Flink 作业完成情况监控架构设计

## 架构概览

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    Flink 作业监控架构全景图                                │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│                      Flink 部署模式层                                     │
├─────────────────┬───────────────┬─────────────────┬─────────────────────┤
│  Local Cluster  │ Remote Cluster│  Yarn Session   │  K8s Session        │
│  (Standalone)   │ (Standalone)  │  Application    │  Application        │
├─────────────────┼───────────────┼─────────────────┼─────────────────────┤
│  - ZK for HA    │  - ZK for HA  │  - Session Mode │  - Native K8s HA    │
│  - REST API     │  - REST API   │  - App Mode     │  - REST API         │
│  - Dispatcher   │  - Dispatcher │  - RM/AM        │  - K8s Service      │
└─────────────────┴───────────────┴─────────────────┴─────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                   HA 领导者选举与发现层                                   │
├─────────────────┬───────────────────┬───────────────────────────────────┤
│   ZooKeeper     │   Kubernetes      │   Other HA Providers              │
│                 │                   │                                   │
│  ┌───────────┐  │  ┌─────────────┐  │  - StandaloneHAServices          │
│  │Leader Path│  │  │K8s Service  │  │  - HadoopHAServices              │
│  │/leader/   │  │  │Discovery    │  │                                   │
│  │jm/*       │  │  │             │  │  Each provides:                  │
│  │           │  │  └─────────────┘  │  - getDispatcherLeaderRetriever()│
│  │ Curator   │  │                   │  - getJobManagerLeaderRetriever()│
│  │ Watcher   │  │                   │  - getClusterRestEndpointLeader()│
│  └───────────┘  │                   │                                   │
│                 │                   │                                   │
│  Watches:       │  Watches:         │  Watches:                         │
│  - Connection   │  - Pod IP Change │  - Leader Changes                 │
│  - Session Loss │  - Service Endpt │  - Session Timeouts               │
│  - Leader Fail  │  - Pod Resched   │  - Connection Loss                │
└─────────────────┴───────────────────┴───────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                      REST API 监控层                                      │
│                                                                        │
│  ┌────────────────────────────────────────────────────────────┐       │
│  │  主要监控接口:                                               │       │
│  │  - /jobs/:jobId/status        (作业状态)                   │       │
│  │  - /jobs/:jobId/exceptions    (异常信息)                   │       │
│  │  - /jobs                      (所有作业列表)                │       │
│  │  - /jobs/:jobId/accumulators  (累加器)                     │       │
│  │  - /joboverview               (作业概览)                   │       │
│  └────────────────────────────────────────────────────────────┘       │
│                                                                        │
│  问题: 这些接口在集群关闭后不可用                                       │
└─────────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                    作业状态持久化归档层                                   │
├─────────────────────────────────────────────────────────────────────────┤
│  JobManager Archivist (实时归档)                                        │
│                                                                        │
│  When job completes, dispatcher archives to:                           │
│  - fs.job-archiver.dir: file:///data/flink/archive/                    │
│  - Format: {jobId}/job-{timestamp}-{state}.json                        │
│                                                                        │
│  Example Archive Files:                                                │
│  {jobId}/                                                              │
│    ├─ joboverview.json          (作业概览)                             │
│    ├─ jobconfig.json           (作业配置)                             │
│    ├─ jacexception.json        (异常信息)                             │
│    ├─ jobvertices.json         (顶点信息)                             │
│    └─ subtasktimes.json        (子任务时间)                           │
│                                                                        │
│  Archive Trigger Conditions:                                           │
│  - JobState: FINISHED, FAILED, CANCELED                               │
│  - Final Checkpoint completed                                         │
│  - Cleanup disabled if: keep-local-files=true                         │
└─────────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                      History Server 层                                  │
│                                                                        │
│  ┌────────────────────────────────────────────────────────────┐       │
│  │  HistoryServerArchiveFetcher                                │       │
│  │  - Polling interval: 10s (configurable)                    │       │
│  │  - Monitors archive directories                            │       │
│  │  - Syncs archives to web-accessible path                   │       │
│  │                                                            │       │
│  │  Key Feature:                                              │       │
│  │  ✓ Works even when Flink cluster is DOWN                   │       │
│  │  ✓ Reads from durable storage (HDFS/S3/NFS)               │       │
│  │  ✓ Provides read-only REST API for completed jobs         │       │
│  └────────────────────────────────────────────────────────────┘       │
│                                                                        │
│  Endpoints (always available):                                         │
│  - /jobs/                            (completed jobs list)           │
│  - /jobs/:jobId                      (job details)                   │
│  - /jobs/:jobId/config               (configuration)                 │
│  - /jobs/:jobId/exceptions           (exceptions)                    │
│  - /jobs/:jobId/vertices/:vid/accumulators (accumulators)           │
│                                                                        │
│  Configuration:                                                        │
│  historyserver.archive.fs.dir: hdfs:///flink/archive                  │
│  historyserver.web.address: 0.0.0.0                                  │
│  historyserver.web.port: 8082                                        │
└─────────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                      监控服务层 (自定义实现)                              │
│                                                                        │
│  ┌────────────────────────────────────────────────────────────┐       │
│  │  JobStatusListener (实现接口)                               │       │
│  │                                                            │       │
│  │  public class CustomJobMonitor implements                  │       │
│  │              JobStatusListener, JobStatusProvider {        │       │
│  │                                                            │       │
│  │    @Override                                              │       │
│  │    public void jobStatusChanges(JobID jobId,              │       │
│  │                             JobStatus newState,           │       │
│  │                             long timestamp) {             │       │
│  │      // 1. Persist to DB (PostgreSQL, MongoDB)           │       │
│  │      // 2. Send to message queue (Kafka, Pulsar)         │       │
│  │      // 3. Trigger webhooks (Slack, DingTalk)            │       │
│  │      // 4. Update metrics (Prometheus pushgateway)        │       │
│  │    }                                                      │       │
│  │  }                                                        │       │
│  └────────────────────────────────────────────────────────────┘       │
│                                                                        │
│  注册到 ExecutionGraph:                                                │
│  executionGraph.registerJobStatusListener(new CustomJobMonitor());    │
└─────────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                    外部监控与告警系统                                     │
│                                                                        │
│  ┌────────────────┐  ┌──────────────┐  ┌─────────────────────────────┐  │
│  │   Prometheus   │  │   InfluxDB   │  │   Elasticsearch + Kibana   │  │
│  │                │  │              │  │                             │  │
│  │ - Metrics      │  │ - Time Series│  │ - Centralized Logging      │  │
│  │ - AlertManager │  │ - Telegraf   │  │ - Job Completion Logs      │  │
│  └────────────────┘  └──────────────┘  └─────────────────────────────┘  │
│                                                                        │
│  ┌────────────────┐  ┌──────────────┐  ┌─────────────────────────────┐  │
│  │   Kafka/Pulsar │  │  PostgreSQL  │  │   Distributed Tracing      │  │
│  │                │  │              │  │   (Jaeger, Zipkin)          │  │
│  │ - Event Stream │  │ - Persist    │  │                             │  │
│  │ - Data Pipeline│  │ - Job Status │  │ - Job Latency Tracking      │  │
│  └────────────────┘  └──────────────┘  └─────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────┘
```

## 边缘情况与解决方案

### 1. Local Standalone + ZK HA 模式

#### 场景描述:
- JobManager 使用 ZK 进行 HA 选举
- 故障转移时 REST 地址会改变
- 批作业完成后集群关闭

#### 边缘情况:
1. **主 JM 故障，备 JM 接管**
   - 症状: REST API 地址从 `jm-node1:8081` 变为 `jm-node2:8081`
   - 解决方案: 使用 `HighAvailabilityServices.getJobManagerLeaderRetriever(JobID)` 动态获取当前 Leader 地址

2. **ZK 集群不可用**
   - 症状: 无法获取 Leader 信息
   - 解决方案:
     - 读取最后一次缓存的 Leader 地址
     - 切换到 HistoryServer 查询已完成作业

3. **集群完全关闭**
   - 症状: 所有 JM 节点都无法访问
   - 解决方案:
     - **首选**: HistoryServer (从 HDFS/S3 读取归档)
     - **备选**: 监控数据库 (JobStatusListener 提前写入)

#### 监控配置:
```yaml
# flink-conf.yaml
high-availability: zookeeper
high-availability.zookeeper.quorum: zk1:2181,zk2:2181,zk3:2181
high-availability.zookeeper.root: /flink
jobmanager.archive.fs.dir: hdfs://namenode:9000/flink/archive
historyserver.archive.fs.dir: hdfs://namenode:9000/flink/archive
historyserver.archive.fs.refresh-interval: 5000
```

---

### 2. Remote Standalone + ZK HA 模式

#### 场景描述:
- 类似 Local 模式，但集群在远程
- 可能通过网络隔离或防火墙访问

#### 边缘情况:
1. **网络分区导致无法访问 JM**
   - 症状: 连接超时或拒绝
   - 解决方案:
     ```java
     // 1. 多次重试 + 指数退避
     RetryPolicy retry = RetryUtils.with ExponentialBackoff(3, 100);
     // 2. 尝试多个备用地址
     List<String> backupAddresses = Arrays.asList(
         "jm-backup1:8081",
         "jm-backup2:8081"
     );
     // 3. 回退到 HistoryServer
     ```

2. **HA 选举延迟**
   - 症状: ZK 选举过程中无 Leader
   - 解决方案: 轮询 `getLeaderRetrievalService().getLeaderGateway().getNow()` 等待 Leader 选举完成

3. **远程集群重启**
   - 症状: JobID 变化或作业丢失
   - 解决方案:
     - 查询 HistoryServer 确认作业是否已完成
     - 对比 JobID 和提交时间匹配作业

#### 监控代码示例:
```java
public class RemoteFlinkMonitor {
    private final HighAvailabilityServices haServices;

    public CompletableFuture<String> getJobManagerAddress(JobID jobId) {
        JobManagerLeaderRetriever retriever =
            haServices.getJobManagerLeaderRetriever(jobId);

        return retriever.getLeaderGateway().thenApply(gateway -> {
            String address = gateway.getGatewayAddress();
            int port = gateway.getGatewayPort();
            return address + ":" + port;
        }).orTimeout(Duration.ofSeconds(10))
          .exceptionally(ex -> {
              // 回退到 HistoryServer
              return historyServerUrl;
          });
    }
}
```

---

### 3. Yarn Session 模式

#### 场景描述:
- 长期运行的 YARN Session
- 多个作业共享同一个 Session

#### 边缘情况:
1. **YARN Session 关闭**
   - 症状: YARN Application ID 不再存在
   - 解决方案:
     ```java
     // 使用 YarnClusterDescriptor 检测集群状态
     YarnClusterDescriptor descriptor = new YarnClusterDescriptor(...);
     try {
         ApplicationReport report = yarnClient.getApplicationReport(appId);
         if (report.getFinalApplicationStatus() != UNDEFINED) {
             // 集群已关闭，切换到 HistoryServer
             throw new ClusterClosedException();
         }
     } catch (ApplicationNotFoundException e) {
         // 集群不存在，使用归档数据
     }
     ```

2. **YARN ResourceManager 故障**
   - 症状: 无法获取 Application 状态
   - 解决方案:
     - 连接到 Standby RM
     - 读取 HistoryServer 缓存的作业信息

3. **YARN NodeManager 故障导致 JM 容器丢失**
   - 症状: JM 容器被 YARN 重新分配
   - 解决方案: HA ZK 会处理 Leader 选举，客户端自动重连

#### 监控配置:
```yaml
# flink-conf.yaml
high-availability: zookeeper
high-availability.zookeeper.quorum: zk:2181
yarn.application-attempts: 10
jobmanager.archive.fs.dir: hdfs://namenode:9000/flink/archive
historyserver.archive.fs.dir: hdfs://namenode:9000/flink/archive
```

---

### 4. Yarn Application 模式

#### 场景描述:
- 每个作业独立运行一个 YARN Application
- 作业完成后 Application 立即结束

#### 边缘情况:
1. **ApplicationMaster 容器关闭**
   - 症状: YARN Application 结束后 JM 立即不可访问
   - 解决方案: **必须依赖 HistoryServer**
     - 配置 `yarn.application-attempts: 1` 快速失败
     - HistoryServer 从 HDFS 读取归档文件
     - 监控代码必须定期同步 HistoryServer

2. **ApplicationMaster 故障转移延迟**
   - 症状: 尝试次数用尽，Application 失败
   - 解决方案:
     - 增加 `yarn.application-attempts` 配置
     - 快速回退到 HistoryServer 查询

3. **作业完成后立即查询**
   - 症状: 作业状态转换到 FINISHED，但 HDFS 归档可能有延迟 (5-10s)
   - 解决方案:
     ```java
     // 轮询检查 HistoryServer
     for (int i = 0; i < 10; i++) {
         Optional<ArchivedJob> job = historyServer.getJob(jobId);
         if (job.isPresent() && job.get().getStatus() == FINISHED) {
             return job.get();
         }
         Thread.sleep(1000);
     }
     ```

#### 监控代码示例:
```java
public class YarnApplicationMonitor {
    private static final Duration TIMEOUT = Duration.ofMinutes(5);

    public JobStatus monitorYarnJob(YarnClient yarnClient, ApplicationId appId) {
        Instant start = Instant.now();

        while (Duration.between(start, Instant.now()).compareTo(TIMEOUT) < 0) {
            try {
                ApplicationReport report = yarnClient.getApplicationReport(appId);

                // 检查 Application 是否还在运行
                if (report.getFinalApplicationStatus() != UNDEFINED) {
                    // Application 已结束，查询 HistoryServer
                    return queryHistoryServer(appId);
                }

                // 查询当前作业状态
                JobStatus status = queryJobManager(report.getHost());
                if (status.isTerminalState()) {
                    // 作业已完成，但可能需要等待归档
                    return waitForArchive(status);
                }

                Thread.sleep(2000); // 轮询间隔
            } catch (Exception e) {
                // 网络错误，回退到 HistoryServer
                return queryHistoryServer(appId);
            }
        }

        // 超时，回退到 HistoryServer
        return queryHistoryServer(appId);
    }

    private JobStatus queryHistoryServer(ApplicationId appId) {
        // 从 HDFS 读取归档文件
        Path archivePath = new Path("/flink/archive/" + appId);
        // 解析 joboverview.json 获取状态
    }
}
```

---

### 5. Kubernetes Session 模式

#### 场景描述:
- 长期运行的 K8s Session
- JM Pod 可能因为故障转移而更换 IP

#### 边缘情况:
1. **Pod 故障转移**
   - 症状: Pod IP 变化或 Pod 被重新调度
   - 解决方案: 使用 K8s Service (ClusterIP 或 LoadBalancer)
     ```yaml
     apiVersion: flink.apache.org/v1beta1
     kind: FlinkDeployment
     metadata:
       name: flink-session
     spec:
       flinkConfiguration:
         high-availability: kubernetes
         kubernetes.service-account: flink
         kubernetes.cluster-id: flink-session
       jobManager:
         replicas: 2  # HA 配置
       taskManager:
         replicas: 4
     ```

2. **K8s Service 不可用**
   - 症状: Service 被删除或网络策略阻止
   - 解决方案:
     - 使用 NodePort 访问备用地址
     - 依赖 HistoryServer

3. **Pod 被删除但集群未关闭**
   - 症状: Deployment 会重新创建 Pod
   - 解决方案: K8s HA 会自动处理，客户端重试连接

---

### 6. Kubernetes Application 模式

#### 场景描述:
- 每个作业独立的 K8s Deployment
- 作业完成后 Deployment 被删除

#### 边缘情况:
1. **Deployment 删除后无法访问**
   - 症状: K8s Service 和 Pod 都被清理
   - 解决方案: **必须使用 HistoryServer**
     - 配置 HDFS/S3 持久化归档
     - 监控服务查询 HistoryServer 而不是 K8s API

2. **JobManager Pod 故障**
   - 症状: Pod CrashLoopBackOff
   - 解决方案: 增加 `restartPolicy: Always`
     ```yaml
     template:
       spec:
         restartPolicy: Always  # 默认就是 Always
         containers:
         - name: jobmanager
           restartPolicy: Never  # JobManager 不重启，由 HA 处理
     ```

3. **作业完成后立即查询**
   - 症状: K8s 清理速度快，REST API 立即不可用
   - 解决方案: 监控代码在提交作业时记录 JobID，直接查询 HistoryServer

#### 监控代码示例:
```java
public class KubernetesApplicationMonitor {
    private final FlinkK8sClient flinkClient;
    private final String clusterId;

    public JobStatus monitorK8sJob(String namespace, String clusterId) {
        // 1. 检查 K8s Deployment 是否存在
        Optional<Deployment> deployment = flinkClient.getDeployment(clusterId);
        if (!deployment.isPresent()) {
            // Deployment 不存在，查询 HistoryServer
            return queryHistoryServer(clusterId);
        }

        // 2. 检查 Pod 状态
        PodStatus podStatus = flinkClient.getJobManagerPodStatus(clusterId);
        if (podStatus.getPhase() == PodPhase.Succeeded ||
            podStatus.getPhase() == PodPhase.Failed) {
            // 作业已完成，查询 HistoryServer
            return queryHistoryServer(clusterId);
        }

        // 3. 查询当前状态
        return queryJobManagerViaService(clusterId);
    }

    private JobStatus queryHistoryServer(String clusterId) {
        // 从 HistoryServer 查询已完成作业
        String hsUrl = "http://history-server:8082/jobs";
        // 过滤 clusterId = ${clusterId} 的作业
    }
}
```

---

## 统一监控解决方案

### 核心原则

1. **三层查询策略**:
   ```
   [REST API] → [ZK/K8s Leader 检索] → [HistoryServer] → [监控数据库]
   ```

2. **自动降级机制**:
   - 优先查询当前 JM REST API
   - 失败时通过 HA 服务获取 Leader 地址
   - HA 不可用时查询 HistoryServer
   - HistoryServer 不可用时查询本地缓存

3. **数据持久化**:
   - 作业提交时: 记录 JobID、ApplicationID、ClusterID
   - 作业运行中: JobStatusListener 实时写入监控数据库
   - 作业完成后: 依赖 HistoryServer 归档，无需额外操作

### 统一监控接口

```java
public interface FlinkJobMonitor {
    /**
     * 获取作业状态，支持所有部署模式
     */
    JobStatus getJobStatus(JobIdentifier jobId);

    /**
     * 获取作业完成信息 (适用于已完成的作业)
     */
    CompletableFuture<JobCompletionInfo> getJobCompletionInfo(JobIdentifier jobId);

    /**
     * 监控作业直到完成
     */
    CompletableFuture<JobCompletionInfo> monitorJobUntilCompletion(
        JobIdentifier jobId, Duration timeout);

    /**
     * 回退查询策略
     */
    default CompletableFuture<JobCompletionInfo> queryWithFallback(JobIdentifier jobId) {
        return queryViaRestApi(jobId)
            .or(() -> queryViaHAServices(jobId))
            .or(() -> queryViaHistoryServer(jobId))
            .or(() -> queryViaDatabase(jobId));
    }
}

public class UnifiedFlinkMonitor implements FlinkJobMonitor {
    private final HighAvailabilityServices haServices;
    private final HistoryServerGateway historyServer;
    private final JobStatusDatabase jobDb;

    @Override
    public JobStatus getJobStatus(JobIdentifier jobId) {
        // 1. 尝试通过 REST API 查询
        try {
            RestClient client = getRestClient(jobId);
            return client.getJobStatus(jobId);
        } catch (Exception e) {
            // 2. REST API 失败，尝试 HA 服务
            try {
                String leaderAddress = haServices.getJobManagerLeader(jobId);
                RestClient client = new RestClient(leaderAddress);
                return client.getJobStatus(jobId);
            } catch (Exception e2) {
                // 3. HA 服务失败，查询 HistoryServer
                return historyServer.getJobStatus(jobId);
            }
        }
    }

    @Override
    public CompletableFuture<JobCompletionInfo> monitorJobUntilCompletion(
            JobIdentifier jobId, Duration timeout) {

        return queryWithFallback(jobId).thenCompose(info -> {
            if (info.getStatus().isTerminalState()) {
                return CompletableFuture.completedFuture(info);
            }

            // 未完成，继续监控
            return monitorPeriodically(jobId, timeout);
        });
    }

    private CompletableFuture<JobCompletionInfo> monitorPeriodically(
            JobIdentifier jobId, Duration timeout) {

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        return CompletableFuture.supplyAsync(() -> {
            Instant start = Instant.now();
            while (Duration.between(start, Instant.now()).compareTo(timeout) < 0) {
                JobCompletionInfo info = queryWithFallback(jobId).join();
                if (info.getStatus().isTerminalState()) {
                    return info;
                }

                try {
                    Thread.sleep(5000); // 5 秒轮询间隔
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            // 超时，返回最后一次查询结果
            return queryWithFallback(jobId).join();
        });
    }
}
```

### 配置建议

#### 1. HistoryServer 必须配置持久化存储

```yaml
# flink-conf.yaml
historyserver.archive.fs.dir: hdfs://namenode:9000/flink/archive
historyserver.archive.fs.refresh-interval: 5000
historyserver.web.address: 0.0.0.0
historyserver.web.port: 8082
jobmanager.archive.fs.dir: hdfs://namenode:9000/flink/archive
```

#### 2. HA 配置

**ZooKeeper:**
```yaml
high-availability: zookeeper
high-availability.zookeeper.quorum: zk1:2181,zk2:2181,zk3:2181
high-availability.zookeeper.root: /flink
high-availability.cluster-id: my-flink-cluster
```

**Kubernetes:**
```yaml
high-availability: kubernetes
kubernetes.cluster-id: my-flink-cluster
kubernetes.service-account: flink
kubernetes.namespace: default
```

#### 3. 监控服务配置

```yaml
# monitoring-config.yaml
monitoring:
  history-server:
    url: http://history-server:8082
    timeout: 30s
    retry-attempts: 3

  ha-services:
    zookeeper:
      quorum: zk:2181
      root-path: /flink
    kubernetes:
      cluster-id: my-cluster

  fallback:
    enabled: true
    primary-timeout: 10s
    secondary-timeout: 20s
    history-server-timeout: 30s

  persistence:
    database:
      type: postgresql
      url: jdbc:postgresql://db:5432/flink_monitor
      table: job_status_history
```

### 最佳实践

1. **作业提交阶段**:
   - 记录 JobID、ApplicationID、ClusterID
   - 立即查询一次初始状态
   - 启动 JobStatusListener 监控

2. **作业运行阶段**:
   - 每 30 秒查询一次状态
   - 异常时立即查询 HistoryServer
   - 记录所有状态转换到数据库

3. **作业完成阶段**:
   - 检测到终端状态后，等待 10 秒归档
   - 查询 HistoryServer 确认最终状态
   - 写入完成事件到消息队列

4. **集群维护阶段**:
   - HistoryServer 必须独立部署，不依赖 Flink 集群
   - 监控服务优先查询 HistoryServer
   - 保留 30 天的归档数据

### 总结

该监控架构通过三层回退机制 (REST API → HA 服务 → HistoryServer) 解决了所有边缘情况：

- ✅ **HA 故障转移**: 通过 HighAvailabilityServices 动态获取 Leader
- ✅ **集群关闭**: HistoryServer 提供独立于集群的只读接口
- ✅ **YARN Application 模式**: 强制使用 HistoryServer
- ✅ **K8s Application 模式**: 强制使用 HistoryServer
- ✅ **网络分区**: 多次重试 + 自动降级
- ✅ **归档延迟**: 轮询等待 + 缓存机制

关键在于 **HistoryServer 必须配置持久化存储**，并且 **监控代码必须实现多层回退查询策略**。
