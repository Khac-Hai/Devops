package com.example.account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@RestController
public class AccountServiceApplication {

    private static final Logger log = LoggerFactory.getLogger(AccountServiceApplication.class);
    private final DataSource dataSource;

    public AccountServiceApplication(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static void main(String[] args) {
        SpringApplication.run(AccountServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner testConnection() {
        return args -> {
            try (Connection conn = dataSource.getConnection()) {
                log.info("=================================================");
                log.info("Account Service - Connected to DB via banking-network!");
                log.info("Database Product Name : {}", conn.getMetaData().getDatabaseProductName());
                log.info("Database URL          : {}", conn.getMetaData().getURL());
                log.info("Database Name / Schema: {}", conn.getCatalog());
                log.info("=================================================");
            } catch (Exception e) {
                log.error("Failed to connect to database: {}", e.getMessage(), e);
            }
        };
    }

    @GetMapping("/")
    public Map<String, Object> root() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "account-service");
        response.put("port", 8080);
        response.put("network", "banking-network");
        try (Connection conn = dataSource.getConnection()) {
            response.put("database", conn.getCatalog());
            response.put("database_status", "CONNECTED");
        } catch (Exception e) {
            response.put("database_status", "ERROR: " + e.getMessage());
        }
        return response;
    }
}
