package com.arvind.config;

import javax.sql.DataSource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JpaConfig {
 
	@Bean
	@Primary
	@ConfigurationProperties(prefix="spring.datasource")
	public DataSource dataSource() {
	    return DataSourceBuilder.create().build();
	}
	
//    @Bean
//    @Primary
//    public DataSource mySqlDataSource() 
//    {
//        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
//        dataSourceBuilder
//        dataSourceBuilder.url("jdbc:mysql://localhost:3306/InnoDB");
//        dataSourceBuilder.username("arvind");
//        dataSourceBuilder.password("delta13");
//        return dataSourceBuilder.build();
//        
//        com.mysql.cj.jdbc.Driver
//    }
}