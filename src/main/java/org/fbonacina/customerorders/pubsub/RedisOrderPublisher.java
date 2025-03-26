package org.fbonacina.customerorders.pubsub;

import org.fbonacina.customerorders.model.OrderMessage;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

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
    redisTemplate.convertAndSend(topic.getTopic(), message);
  }
}
