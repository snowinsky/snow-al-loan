package cn.snow.loan.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.github.braisdom.objsql.ConnectionFactory;
import com.github.braisdom.objsql.Databases;

public enum DbHandler {

    INIT;

    public void initMySqlConnectionFactory(){
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


}
