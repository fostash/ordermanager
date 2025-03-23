package org.fbonacina.customerorders.utils;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

public interface BaseITTest {
  @Container
  MariaDBContainer<?> mariaDb = new MariaDBContainer<>(DockerImageName.parse("mariadb:11.4"));

  @DynamicPropertySource
  static void overrideProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", mariaDb::getJdbcUrl);
    registry.add("spring.datasource.username", mariaDb::getUsername);
    registry.add("spring.datasource.password", mariaDb::getPassword);
    registry.add("spring.flyway.url", mariaDb::getJdbcUrl);
    registry.add("spring.flyway.user", mariaDb::getUsername);
    registry.add("spring.flyway.password", mariaDb::getPassword);
    registry.add("spring.flyway.driver", mariaDb::getDriverClassName);
  }
}
