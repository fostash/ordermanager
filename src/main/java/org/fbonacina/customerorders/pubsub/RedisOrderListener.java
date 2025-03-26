package org.fbonacina.customerorders.pubsub;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.fbonacina.customerorders.services.MeilisearchService;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RedisOrderListener implements MessageListener {

  private final MeilisearchService meilisearchService;

  public RedisOrderListener(MeilisearchService meilisearchService) {
    this.meilisearchService = meilisearchService;
  }

  public void onMessage(Message message, byte[] pattern) {
    log.info("Message received: {}", message);
    try {
      meilisearchService.indexOrder(message.toString());
    } catch (JsonProcessingException e) {
      log.error("error on indexing order to meilisearch. message: {}", message);
      // throw new RuntimeException(e);
    }
  }
}
