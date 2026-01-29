package cn.iocoder.yudao.module.flink.deploy.util.sql;

import static cn.iocoder.yudao.module.flink.deploy.util.sql.FlinkSqlScriptExecutor.execute;

import org.apache.flink.table.api.bridge.java.StreamStatementSet;
import org.apache.flink.table.api.internal.TableEnvironmentImpl;

/**
 * SQL 执行工具类
 *
 * <p>提供 SQL 脚本执行功能，内部委托给 {@link FlinkSqlScriptExecutor}。</p>
 *
 * @author yzh
 */
public class SqlUtil {

  /**
   * 处理 SQL 脚本
   *
   * <p>将 SQL 脚本解析并执行，支持：
   * <ul>
   *   <li>DML 语句 - INSERT INTO</li>
   *   <li>DDL 语句 - CREATE/DROP/ALTER TABLE 等</li>
   *   <li>配置语句 - SET/RESET</li>
   *   <li>USE 语句</li>
   * </ul>
   *
   * @param sqlScripts    SQL 脚本内容
   * @param tbEnv         TableEnvironment 实例
   * @param statementSet  StatementSet 实例
   * @return TableEnvironmentImpl 实例
   * @deprecated 请使用 {@link FlinkSqlScriptExecutor#execute(String, TableEnvironmentImpl, StreamStatementSet)}
   */
  @Deprecated
  public static TableEnvironmentImpl processScripts(
      String sqlScripts, TableEnvironmentImpl tbEnv, StreamStatementSet statementSet) {
    execute(sqlScripts, tbEnv, statementSet);
    return tbEnv;
  }
}
