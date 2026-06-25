package com.deutschhub;

import com.deutschhub.infrastructure.config.AdminAccountProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;

@ConfigurationPropertiesScan
@SpringBootApplication
@EnableConfigurationProperties(AdminAccountProperties.class)
public class DeutschHubApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(DeutschHubApplication.class, args);
    }
}