package cn.iocoder.yudao.module.flink.deploy.util.sql;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import org.apache.flink.table.api.bridge.java.StreamStatementSet;
import org.apache.flink.table.api.internal.TableEnvironmentImpl;
import org.apache.flink.table.operations.ModifyOperation;
import org.apache.flink.table.operations.Operation;
import org.apache.flink.table.operations.ShowOperation;
import org.apache.flink.table.planner.operations.PlannerQueryOperation;

/**
 * @author yzh
 */
public class SqlUtil {
  public static TableEnvironmentImpl processScripts(
      String sqlScripts, TableEnvironmentImpl tbEnv, StreamStatementSet statementSet) {
    boolean useStatementSet = false;
    String[] statements =
        cn.iocoder.yudao.module.flink.common.util.SqlUtil.getStatements(sqlScripts);
    for (String statement : statements) {
      Operation operation = tbEnv.getParser().parse(statement).get(0);
      if (operation instanceof ModifyOperation) {
        statementSet.addInsertSql(statement);
        useStatementSet = true;
      } else if (operation instanceof ShowOperation || operation instanceof PlannerQueryOperation) {
        throw ServiceExceptionUtil.exception(
            new ErrorCode(9999, "不支持的Show SQL 或 SELECT SQL 类型{}"), operation.getClass().getName());
      } else {
        tbEnv.executeSql(statement);
      }
    }
    if (useStatementSet) {
      statementSet.attachAsDataStream();
    }

    return tbEnv;
  }
}
