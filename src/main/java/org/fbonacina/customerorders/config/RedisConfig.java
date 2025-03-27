package org.fbonacina.customerorders.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.fbonacina.customerorders.messages.OrderMessage;
import org.fbonacina.customerorders.pubsub.OrderPublisher;
import org.fbonacina.customerorders.pubsub.RedisOrderListener;
import org.fbonacina.customerorders.pubsub.RedisOrderPublisher;
import org.fbonacina.customerorders.services.MeilisearchService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

@Configuration
public class RedisConfig {

  private final RedisConnectionFactory connectionFactory;

  @Value("${meilisearch.ordersTopic}")
  private String ordersTopic;

  public RedisConfig(RedisConnectionFactory connectionFactory) {
    this.connectionFactory = connectionFactory;
  }

  @Bean
  public ObjectMapper objectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());

    return objectMapper;
  }

  @Bean
  public RedisTemplate<String, OrderMessage> redisTemplate() {
    var template = new RedisTemplate<String, OrderMessage>();
    template.setConnectionFactory(connectionFactory);
    template.setValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper()));
    return template;
  }

  @Bean
  public MessageListenerAdapter messageListener(MeilisearchService meilisearchService) {
    return new MessageListenerAdapter(new RedisOrderListener(meilisearchService));
  }

  @Bean
  public RedisMessageListenerContainer redisContainer(
      MessageListenerAdapter messageListenerAdapter) {
    RedisMessageListenerContainer container = new RedisMessageListenerContainer();
    container.setConnectionFactory(connectionFactory);
    container.addMessageListener(messageListenerAdapter, topic());
    return container;
  }

  @Bean
  public OrderPublisher redisPublisher() {
    return new RedisOrderPublisher(redisTemplate(), topic());
  }

  @Bean
  public ChannelTopic topic() {
    return new ChannelTopic(ordersTopic);
  }
}
