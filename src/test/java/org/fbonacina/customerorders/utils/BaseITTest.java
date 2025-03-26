package org.fbonacina.customerorders.utils;

import com.redis.testcontainers.RedisContainer;
import io.vanslog.testcontainers.meilisearch.MeilisearchContainer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

public interface BaseITTest {
  @Container
  MariaDBContainer<?> mariaDb = new MariaDBContainer<>(DockerImageName.parse("mariadb:11.4"));

  @Container
  RedisContainer REDIS_CONTAINER =
      new RedisContainer(DockerImageName.parse("redis:5.0.3-alpine")).withExposedPorts(6379);

  @Container
  MeilisearchContainer meilisearch =
      new MeilisearchContainer(DockerImageName.parse("getmeili/meilisearch:latest"))
          .withMasterKey("masterKey");

  @DynamicPropertySource
  private static void registerContainersProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
    registry.add("spring.data.redis.port", () -> REDIS_CONTAINER.getMappedPort(6379).toString());

    registry.add("spring.datasource.url", mariaDb::getJdbcUrl);
    registry.add("spring.datasource.username", mariaDb::getUsername);
    registry.add("spring.datasource.password", mariaDb::getPassword);
    registry.add("spring.flyway.url", mariaDb::getJdbcUrl);
    registry.add("spring.flyway.user", mariaDb::getUsername);
    registry.add("spring.flyway.password", mariaDb::getPassword);
    registry.add("spring.flyway.driver", mariaDb::getDriverClassName);

    registry.add(
        "meilisearch.url",
        () -> "http://" + meilisearch.getHost() + ":" + meilisearch.getMappedPort(7700));
  }
}
