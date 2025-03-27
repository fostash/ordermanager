package org.fbonacina.customerorders.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meilisearch.sdk.Client;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.fbonacina.customerorders.messages.OrderMessage;
import org.fbonacina.customerorders.utils.BaseITTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class MeilisearchServiceTest implements BaseITTest {

  @Autowired private RedisTemplate<String, OrderMessage> redisTemplate;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private Client client;

  @Autowired private MeilisearchService meilisearchService;

  @Value("${meilisearch.ordersIndex}")
  private String ordersIndex;

  @Value("${meilisearch.ordersTopic}")
  private String ordersTopic;

  @Test
  public void testOnMessage() throws Exception {
    OrderMessage orderEvent =
        OrderMessage.builder()
            .orderId(1L)
            .orderDate(LocalDate.now().format(DateTimeFormatter.ISO_DATE))
            .orderName("test")
            .orderDescription("test")
            .build();
    redisTemplate.convertAndSend(ordersTopic, orderEvent);
    Thread.sleep(3000);

    var result = client.index(ordersIndex).getDocuments(OrderMessage.class);
    assertEquals(1, result.getTotal());
    assertEquals(orderEvent.orderDescription(), result.getResults()[0].orderDescription());

    OrderMessage updatedOrderEvent =
        OrderMessage.builder()
            .orderId(1L)
            .orderDate(LocalDate.now().format(DateTimeFormatter.ISO_DATE))
            .orderName("test")
            .build();
    redisTemplate.convertAndSend(ordersTopic, updatedOrderEvent);
    Thread.sleep(3000);

    result = client.index(ordersIndex).getDocuments(OrderMessage.class);
    assertEquals(1, result.getTotal());
    assertNull(result.getResults()[0].orderDescription());

    OrderMessage otherOrderEvent =
        OrderMessage.builder()
            .orderId(2L)
            .orderDate(LocalDate.now().format(DateTimeFormatter.ISO_DATE))
            .orderName("test2")
            .orderDescription("test")
            .build();

    redisTemplate.convertAndSend(ordersTopic, otherOrderEvent);
    Thread.sleep(3000);

    result = client.index(ordersIndex).getDocuments(OrderMessage.class);
    assertEquals(2, result.getTotal());
  }

  @Test
  public void testSearch() throws InterruptedException {
    OrderMessage orderEvent1 =
        OrderMessage.builder()
            .orderId(1L)
            .orderDate(LocalDate.now().format(DateTimeFormatter.ISO_DATE))
            .orderName("test")
            .orderDescription("test")
            .build();
    redisTemplate.convertAndSend(ordersTopic, orderEvent1);
    OrderMessage orderEvent2 =
        OrderMessage.builder()
            .orderId(2L)
            .orderDate(LocalDate.now().format(DateTimeFormatter.ISO_DATE))
            .orderName("test")
            .orderDescription("test")
            .build();
    redisTemplate.convertAndSend(ordersTopic, orderEvent2);
    OrderMessage orderEvent3 =
        OrderMessage.builder()
            .orderId(3L)
            .orderDate(LocalDate.of(2010, 10, 12).format(DateTimeFormatter.ISO_DATE))
            .orderName("test")
            .orderDescription("test")
            .build();
    redisTemplate.convertAndSend(ordersTopic, orderEvent3);

    Thread.sleep(3000);

    var searchByOrderName = meilisearchService.search(null, null, null, "test");
    assertEquals(3, searchByOrderName.size());
  }
}
