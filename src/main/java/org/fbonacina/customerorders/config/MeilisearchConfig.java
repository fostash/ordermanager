package org.fbonacina.customerorders.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Config;
import com.meilisearch.sdk.model.Settings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MeilisearchConfig {

  @Value("${meilisearch.url}")
  private String url;

  @Value("${meilisearch.apiKey}")
  private String apiKey;

  @Value("${meilisearch.ordersIndex}")
  private String ordersIndex;

  @Bean
  public Client meiliClient(ObjectMapper objectMapper) {
    var client = new Client(new Config(url, apiKey));

    var settings = new Settings();
    settings.setFilterableAttributes(new String[] {"dateFrom", "dateTo", "orderName", "username"});
    client.index(ordersIndex).updateSettings(settings);

    return client;
  }
}
