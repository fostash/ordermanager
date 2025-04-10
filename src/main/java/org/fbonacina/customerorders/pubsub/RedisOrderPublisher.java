package org.fbonacina.customerorders.pubsub;

import lombok.extern.slf4j.Slf4j;
import org.fbonacina.customerorders.messages.OrderMessage;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RedisOrderPublisher implements OrderPublisher {

  private final RedisTemplate<String, OrderMessage> redisTemplate;
  private final ChannelTopic topic;

  public RedisOrderPublisher(
      RedisTemplate<String, OrderMessage> redisTemplate, ChannelTopic topic) {
    this.redisTemplate = redisTemplate;
    this.topic = topic;
  }

  public void publish(OrderMessage message) {
    log.debug("publish message {} to {}", message, topic.getTopic());
    redisTemplate.convertAndSend(topic.getTopic(), message);
  }
}
