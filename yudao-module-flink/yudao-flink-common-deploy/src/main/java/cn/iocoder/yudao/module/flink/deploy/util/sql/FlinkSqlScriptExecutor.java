package cn.iocoder.yudao.module.flink.deploy.util.sql;

import static cn.iocoder.yudao.module.flink.common.util.SqlUtil.getStatements;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.table.api.bridge.java.StreamStatementSet;
import org.apache.flink.table.api.internal.TableEnvironmentImpl;
import org.apache.flink.table.operations.ModifyOperation;
import org.apache.flink.table.operations.Operation;
import org.apache.flink.table.operations.ShowOperation;
import org.apache.flink.table.planner.operations.PlannerQueryOperation;

/**
 * Flink SQL Script Executor
 *
 * <p>Provides complete SQL script execution capabilities:
 *
 * <ul>
 *   <li>DML statements - INSERT INTO collected to StatementSet
 *   <li>DDL statements - CREATE/DROP/ALTER TABLE/VIEW/CATALOG/FUNCTION
 *   <li>Config statements - SET/RESET (temporary, only for current script)
 *   <li>USE statements - USE CATALOG/DB
 *   <li>MODULE statements - LOAD/UNLOAD MODULE
 * </ul>
 *
 * @author yzh
 */
public class FlinkSqlScriptExecutor {

  // SET statement pattern: SET 'key' = 'value' or SET key = value
  private static final Pattern SET_PATTERN =
      Pattern.compile("(?i)^\\s*SET\\s+['\"]?(\\S+?)['\"]?\\s*=\\s*['\"]?(.+?)['\"]?\\s*$");

  // RESET statement pattern: RESET 'key' or RESET
  private static final Pattern RESET_PATTERN =
      Pattern.compile("(?i)^\\s*RESET\\s+['\"]?(\\S+?)['\"]?\\s*$");

  /**
   * Execute SQL script
   *
   * @param sqlScript SQL script content
   * @param tableEnv TableEnvironment instance
   * @param statementSet StatementSet instance
   * @return Execution result
   */
  public static ExecutionResult execute(
      String sqlScript, TableEnvironmentImpl tableEnv, StreamStatementSet statementSet) {
    boolean hasModifyOperation = false;

    // Track keys modified by SET and their original values
    Map<String, String> originalValues = new HashMap<>();
    Configuration config = tableEnv.getConfig().getConfiguration();

    ScriptIterator iterator = new ScriptIterator(sqlScript);
    while (iterator.hasNext()) {
      String statement = iterator.next();
      if (statement == null || statement.trim().isEmpty()) {
        continue;
      }

      try {
        // Check SET/RESET statement first
        if (handleSetStatement(tableEnv, statement, originalValues, config)) {
          continue;
        }

        Operation operation = parseOperation(tableEnv, statement);
        if (executeOperation(tableEnv, statementSet, operation, statement)) {
          hasModifyOperation = true;
        }
      } catch (Exception e) {
        throw ServiceExceptionUtil.exception(
            new ErrorCode(9999, "Failed to execute SQL statement: {}"), e.getMessage());
      }
    }

    // Restore original configuration values
    for (Map.Entry<String, String> entry : originalValues.entrySet()) {
      if (entry.getValue() == null) {
        // Key was not originally present, need to remove it
        // Since we can't remove by string in Flink 1.18, we set to empty
        config.setString(entry.getKey(), "");
      } else {
        // Restore original value
        config.setString(entry.getKey(), entry.getValue());
      }
    }

    // If there are ModifyOperations, attach as DataStream
    if (hasModifyOperation) {
      statementSet.attachAsDataStream();
    }

    return new ExecutionResult(hasModifyOperation);
  }

  /**
   * Handle SET statement - temporarily set configuration
   *
   * @param tableEnv TableEnvironment instance
   * @param statement SET statement
   * @param originalValues Map to store original values
   * @param config Configuration to modify
   * @return true if SET statement was handled
   */
  private static boolean handleSetStatement(
      TableEnvironmentImpl tableEnv,
      String statement,
      Map<String, String> originalValues,
      Configuration config) {
    Matcher setMatcher = SET_PATTERN.matcher(statement);
    if (setMatcher.matches()) {
      String key = setMatcher.group(1).trim();
      String value = setMatcher.group(2).trim();
      // Remove quotes if present
      if ((value.startsWith("'") && value.endsWith("'"))
          || (value.startsWith("\"") && value.endsWith("\""))) {
        value = value.substring(1, value.length() - 1);
      }

      // Save original value if not already tracked
      if (!originalValues.containsKey(key)) {
        originalValues.put(key, config.getString(key, null));
      }

      config.setString(key, value);
      return true;
    }

    Matcher resetMatcher = RESET_PATTERN.matcher(statement);
    if (resetMatcher.matches()) {
      String key = resetMatcher.group(1).trim();
      if ("".equals(key)) {
        // RESET clears all configuration
        // Save all current values first
        for (String configKey : config.keySet()) {
          if (!originalValues.containsKey(configKey)) {
            originalValues.put(configKey, config.getString(configKey, null));
          }
        }
        // Clear by setting all to empty (workaround for Flink 1.18 limitation)
        for (String configKey : new ArrayList<>(config.keySet())) {
          config.setString(configKey, "");
        }
      } else {
        // Remove quotes
        if ((key.startsWith("'") && key.endsWith("'"))
            || (key.startsWith("\"") && key.endsWith("\""))) {
          key = key.substring(1, key.length() - 1);
        }
        // Save original value
        if (!originalValues.containsKey(key)) {
          originalValues.put(key, config.getString(key, null));
        }
        // Remove by setting to empty
        config.setString(key, "");
      }
      return true;
    }

    return false;
  }

  /** Parse SQL operation */
  private static Operation parseOperation(TableEnvironmentImpl tableEnv, String statement) {
    List<Operation> operations = tableEnv.getParser().parse(statement);
    if (operations.isEmpty()) {
      throw ServiceExceptionUtil.exception(
          new ErrorCode(9999, "Cannot parse SQL statement: {}"), statement);
    }
    return operations.get(0);
  }

  /** Execute operation and return whether it is ModifyOperation */
  private static boolean executeOperation(
      TableEnvironmentImpl tableEnv,
      StreamStatementSet statementSet,
      Operation operation,
      String statement) {

    // DML - INSERT/UPDATE/DELETE
    if (operation instanceof ModifyOperation) {
      statementSet.addInsertSql(statement);
      return true;
    }

    // SHOW statement - throw exception
    if (operation instanceof ShowOperation) {
      throw ServiceExceptionUtil.exception(
          new ErrorCode(9999, "Unsupported SHOW SQL type: {}"), operation.getClass().getName());
    }

    // Query operation - throw exception
    if (operation instanceof PlannerQueryOperation) {
      throw ServiceExceptionUtil.exception(
          new ErrorCode(9999, "Unsupported SELECT SQL type: {}"), operation.getClass().getName());
    }

    // For other operations (DDL, USE, etc.), use executeSql
    tableEnv.executeSql(statement);
    return false;
  }

  /**
   * SQL script iterator
   *
   * <p>Reference: Flink ScriptExecutor.ResultIterator implementation. Provides robust SQL parsing
   * capabilities:
   *
   * <ul>
   *   <li>Handle single-quote strings correctly
   *   <li>Handle double-quote identifiers correctly
   *   <li>Handle backtick identifiers correctly
   *   <li>Handle single-line comments
   *   <li>Handle multi-line comments
   *   <li>Handle consecutive semicolons
   * </ul>
   */
  public static class ScriptIterator implements Iterator<String> {

    private final List<String> statements;
    private final int size;
    private int index = 0;

    public ScriptIterator(String sqlScript) {
      this.statements = splitStatements(sqlScript);
      this.size = statements.size();
    }

    @Override
    public boolean hasNext() {
      return index < size;
    }

    @Override
    public String next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      return statements.get(index++);
    }

    /**
     * Split SQL script into individual statements.
     *
     * <p>Processing logic:
     *
     * <ol>
     *   <li>Skip multi-line comments /star star/
     *   <li>Skip single-line comments dash dash
     *   <li>Handle semicolons inside strings
     *   <li>Split by semicolon, handle consecutive semicolons
     * </ol>
     */
    private List<String> splitStatements(String sql) {
      return Arrays.asList(getStatements(sql));
    }

    /** Skip multi-line comment */
    private int skipMultiLineComment(String sql, int start) {
      int i = start;
      int length = sql.length();
      while (i < length) {
        if (sql.charAt(i) == '*' && i + 1 < length && sql.charAt(i + 1) == '/') {
          return i + 2;
        }
        i++;
      }
      return length;
    }

    /** Skip single-line comment */
    private int skipSingleLineComment(String sql, int start) {
      int i = start;
      int length = sql.length();
      while (i < length) {
        char c = sql.charAt(i);
        if (c == '\n' || c == '\r') {
          return i;
        }
        i++;
      }
      return length;
    }

    /** Extract quoted string including escaped quotes */
    private int extractQuotedString(String sql, int start, char quote, StringBuilder sb) {
      sb.append(quote);
      int i = start;
      int length = sql.length();

      while (i < length) {
        char c = sql.charAt(i);

        // Check for escaped quote '' or ""
        if (c == quote && i + 1 < length && sql.charAt(i + 1) == quote) {
          // Escaped quote - only append one quote, skip both
          sb.append(quote);
          i += 2;
          continue;
        }

        sb.append(c);

        // Found closing quote (not escaped)
        if (c == quote) {
          return i + 1;
        }
        i++;
      }
      return length;
    }
  }

  /** Execution result */
  public static class ExecutionResult {
    private final boolean hasModifyOperation;

    public ExecutionResult(boolean hasModifyOperation) {
      this.hasModifyOperation = hasModifyOperation;
    }

    public boolean hasModifyOperation() {
      return hasModifyOperation;
    }
  }
}
