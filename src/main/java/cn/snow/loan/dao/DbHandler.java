package cn.snow.loan.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;

import com.github.braisdom.objsql.ConnectionFactory;
import com.github.braisdom.objsql.Databases;
import com.github.braisdom.objsql.Logger;
import com.github.braisdom.objsql.LoggerFactory;
import com.github.braisdom.objsql.util.StringUtil;

public enum DbHandler {

    INIT;

    public void initMySqlConnectionFactory() {
        Databases.installLoggerFactory(new Slf4jLoggerFactoryImpl());
        Databases.installConnectionFactory(new MySQLConnectionFactory());
    }

    private static class MySQLConnectionFactory implements ConnectionFactory {

        @Override
        public Connection getConnection(String dataSourceName) throws SQLException {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
                String url = "jdbc:mysql://localhost:3306/al_loan";
                String user = "root";
                String password = "123456";
                return DriverManager.getConnection(url, user, password);
            } catch (SQLException e) {
                throw e;
            } catch (Exception e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }

    public static class Slf4jLoggerFactoryImpl implements LoggerFactory {

        private static class Slf4jLoggerImpl implements Logger {

            private final org.slf4j.Logger logger;

            public Slf4jLoggerImpl(org.slf4j.Logger logger) {
                this.logger = logger;
            }

            @Override
            public void debug(long elapsedTime, String sql, Object[] params) {
                logger.debug(createLogContent(elapsedTime, sql, params));
            }

            @Override
            public void info(long elapsedTime, String sql, Object[] params) {
                logger.info(createLogContent(elapsedTime, sql, params));
            }

            @Override
            public void error(String message, Throwable throwable) {
                logger.error(message, throwable);
            }

            private String createLogContent(long elapsedTime, String sql, Object[] params) {
                String[] paramStrings = Arrays.stream(params)
                        .map(param -> String.valueOf(param)).toArray(String[]::new);
                String paramString = String.join(",", paramStrings);
                return String.format("[%dms] %s, with: [%s]",
                        elapsedTime, sql, String.join(",",
                                paramString.length() > 100 ? StringUtil
                                        .truncate(paramString, 99) : paramString));
            }
        }

        @Override
        public Logger create(Class<?> clazz) {
            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(clazz);
            return new Slf4jLoggerImpl(logger);
        }
    }


}
