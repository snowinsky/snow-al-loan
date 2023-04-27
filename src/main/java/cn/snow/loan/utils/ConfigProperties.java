package cn.snow.loan.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigProperties {

    private String driverClass;
    private String url;
    private String user;
    private String password;

    public ConfigProperties() {
        InputStream in = getClass().getClassLoader().getResourceAsStream("al-loan.properties");
        Properties p = new Properties();
        try {
            p.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("config file al-loan.properties notfound", e);
        }
        driverClass = p.getProperty("database.driver.class");
        url = p.getProperty("database.url");
        user = p.getProperty("database.user");
        password = p.getProperty("database.password");
    }

    public String getDriverClass() {
        return driverClass;
    }

    public void setDriverClass(String driverClass) {
        this.driverClass = driverClass;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
