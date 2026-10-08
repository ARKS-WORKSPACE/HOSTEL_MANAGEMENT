package com.hostelmanagement.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;

@Configuration
public class DataSourceConfig {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceConfig.class);

    @Value("${spring.datasource.url:jdbc:mysql://localhost:3306/hostel_management?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true}")
    private String mysqlUrl;

    @Value("${spring.datasource.username:root}")
    private String mysqlUsername;

    @Value("${spring.datasource.password:root}")
    private String mysqlPassword;

    @Value("${spring.datasource.driver-class-name:com.mysql.cj.jdbc.Driver}")
    private String mysqlDriverClassName;

    @Bean
    @Primary
    public DataSource dataSource() {
        // Attempt MySQL connection first
        try {
            logger.info("Attempting to connect to primary MySQL database at: {}", mysqlUrl);
            Class.forName(mysqlDriverClassName);
            try (Connection conn = DriverManager.getConnection(mysqlUrl, mysqlUsername, mysqlPassword)) {
                logger.info("Successfully connected to MySQL database: {}", mysqlUrl);
                return DataSourceBuilder.create()
                        .url(mysqlUrl)
                        .username(mysqlUsername)
                        .password(mysqlPassword)
                        .driverClassName(mysqlDriverClassName)
                        .build();
            }
        } catch (Exception e) {
            logger.warn("MySQL connection could not be established ({}: {}).", 
                    e.getClass().getSimpleName(), e.getMessage());
            logger.info("--> Activating Development H2 Fallback Database (MODE=MySQL) with persistent storage in ./data/hostel_management.");
            logger.info("--> To switch to MySQL, update spring.datasource.password in application.properties and restart.");

            String h2Url = "jdbc:h2:file:./data/hostel_management;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_DELAY=-1";
            return DataSourceBuilder.create()
                    .url(h2Url)
                    .username("sa")
                    .password("")
                    .driverClassName("org.h2.Driver")
                    .build();
        }
    }
}
