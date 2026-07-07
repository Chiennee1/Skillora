package com.example.skillora_platform.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {
                "spring.flyway.enabled=true",
                "spring.jpa.hibernate.ddl-auto=validate",
                "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect",
                "spring.jpa.show-sql=false",
                "spring.cache.type=simple",
                "management.health.redis.enabled=false"
        }
)
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class FlywayMySqlSchemaValidationTest {

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("Skillora")
            .withUsername("skillora")
            .withPassword("skillora");

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.datasource.driver-class-name", MYSQL::getDriverClassName);
    }

    @Test
    void contextLoadsWithFlywayMySqlAndHibernateValidate() {
    }
}
