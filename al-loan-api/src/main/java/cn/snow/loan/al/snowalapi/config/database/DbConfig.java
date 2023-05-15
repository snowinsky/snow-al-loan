package cn.snow.loan.al.snowalapi.config.database;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cn.snow.loan.dao.DbHandler;

@Configuration
public class DbConfig {

    @Bean
    public String dbInit(){
        DbHandler.INIT.initMySqlConnectionFactory();
        return "";
    }

}
