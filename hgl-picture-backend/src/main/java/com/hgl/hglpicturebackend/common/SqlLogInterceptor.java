package com.hgl.hglpicturebackend.common;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;

/**
 * @ClassName: SqlLogInterceptor
 * @Package: com.hgl.hglpicturebackend.common
 * @Description:
 * @Author HGL
 * @Create: 2025/10/15 15:12
 */
@Component
@Slf4j
@Intercepts({
        @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})
})
public class SqlLogInterceptor implements Interceptor {


    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");

        String id = mappedStatement.getId();
        String sqlCommandType = mappedStatement.getSqlCommandType().toString();
        BoundSql boundSql = statementHandler.getBoundSql();
        String fullSql = getSql(mappedStatement.getConfiguration(), boundSql, id);

        // 过滤掉不希望打印的 SQL
        if (!fullSql.contains("websocket")) {
            // 使用 String.format 和颜色常量构建彩色日志
            String sqlLog = String.format(
                    "\n" +
                            AnsiColor.CYAN.getCode() + "======================================================== SQL Start ========================================================" + AnsiColor.RESET.getCode() + "\n" +
                            AnsiColor.YELLOW.getCode() + "  Type: " + AnsiColor.MAGENTA.getCode() + "%s\n" +
                            AnsiColor.YELLOW.getCode() + "  ID:   " + AnsiColor.GREEN.getCode() + "%s\n" +
                            AnsiColor.YELLOW.getCode() + "  Sql:  " + AnsiColor.RESET.getCode() + AnsiColor.BLUE.getCode() + "%s" + AnsiColor.RESET.getCode() + "\n" +
                            AnsiColor.CYAN.getCode() + "========================================================== SQL End =========================================================" + AnsiColor.RESET.getCode(),
                    sqlCommandType, id, fullSql
            );
            log.info(sqlLog);
        }
        return invocation.proceed();
    }

    /**
     * 获取完整的 SQL 语句，替换占位符为实际参数值。
     *
     * @param configuration MyBatis 配置
     * @param boundSql      包含 SQL 和参数信息的对象
     * @param sqlId         SQL 的唯一标识
     * @return 格式化后的完整 SQL 字符串
     */
    private String getSql(Configuration configuration, BoundSql boundSql, String sqlId) {
        return showSql(configuration, boundSql);
    }

    /**
     * 将参数对象转换为适合 SQL 的字符串形式。
     * <p>
     * 修复：SimpleDateFormat 非线程安全，每次调用都创建新实例。
     *
     * @param obj 参数对象
     * @return 格式化后的参数字符串
     */
    private String getParameterValue(Object obj) {
        if (obj == null) {
            return "''";
        }
        if (obj instanceof String) {
            return "'" + obj + "'";
        }
        if (obj instanceof Date) {
            // 修复线程安全问题：每次都创建新的 SimpleDateFormat 实例
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return "'" + formatter.format((Date) obj) + "'";
        }
        return obj.toString();
    }

    /**
     * 进行 ? 占位符的替换，生成可执行的 SQL。
     *
     * @param configuration MyBatis 配置
     * @param boundSql      包含 SQL 和参数信息的对象
     * @return 完整的 SQL 语句
     */
    public String showSql(Configuration configuration, BoundSql boundSql) {
        Object parameterObject = boundSql.getParameterObject();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        String sql = boundSql.getSql().replaceAll("\\s+", " ").trim();

        if (parameterMappings == null || parameterObject == null) {
            return sql;
        }

        TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
        if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
            sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(getParameterValue(parameterObject)));
        } else {
            MetaObject metaObject = configuration.newMetaObject(parameterObject);
            for (ParameterMapping parameterMapping : parameterMappings) {
                String propertyName = parameterMapping.getProperty();
                if (metaObject.hasGetter(propertyName)) {
                    Object obj = metaObject.getValue(propertyName);
                    sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(getParameterValue(obj)));
                } else if (boundSql.hasAdditionalParameter(propertyName)) {
                    Object obj = boundSql.getAdditionalParameter(propertyName);
                    sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(getParameterValue(obj)));
                } else {
                    // 提醒参数缺失，防止占位符错位
                    sql = sql.replaceFirst("\\?", "MISSING_PARAM");
                }
            }
        }
        return sql;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // 可以在这里从配置文件读取属性
    }
}
