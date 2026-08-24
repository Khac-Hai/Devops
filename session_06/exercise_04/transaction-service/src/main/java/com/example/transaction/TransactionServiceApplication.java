package com.example.transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@RestController
public class TransactionServiceApplication {

    private static final Logger log = LoggerFactory.getLogger(TransactionServiceApplication.class);

    private final DataSource dataSource;

    @Value("${account.service.url:http://account-service:8080}")
    private String accountServiceUrl;

    public TransactionServiceApplication(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static void main(String[] args) {
        SpringApplication.run(TransactionServiceApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public CommandLineRunner init() {
        return args -> {
            try (Connection conn = dataSource.getConnection()) {
                log.info("=================================================");
                log.info("Transaction Service - Connected to DB via banking-network!");
                log.info("Target Account Service URL: {}", accountServiceUrl);
                log.info("Database URL              : {}", conn.getMetaData().getURL());
                log.info("Database Name / Schema    : {}", conn.getCatalog());
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
        response.put("service", "transaction-service");
        response.put("port", 8081);
        response.put("network", "banking-network");
        response.put("accountServiceUrl", accountServiceUrl);
        try (Connection conn = dataSource.getConnection()) {
            response.put("database", conn.getCatalog());
            response.put("database_status", "CONNECTED");
        } catch (Exception e) {
            response.put("database_status", "ERROR: " + e.getMessage());
        }
        return response;
    }
}
