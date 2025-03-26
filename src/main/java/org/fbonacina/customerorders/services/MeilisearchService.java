package org.fbonacina.customerorders.services;

import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.SearchRequest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.fbonacina.customerorders.model.OrderMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MeilisearchService {

  private final Client client;
  private final ObjectMapper objectMapper;
  private final String ordersIndex;

  public MeilisearchService(
      Client client,
      ObjectMapper objectMapper,
      @Value("${meilisearch.ordersIndex}") String ordersIndex) {
    this.client = client;
    this.objectMapper = objectMapper;
    this.ordersIndex = ordersIndex;
  }

  public void indexOrder(String message) throws JsonProcessingException {
    var index = client.index(ordersIndex);
    index.addDocuments(message);
  }

  public List<OrderMessage> search(
      LocalDate dateFrom, LocalDate dateTo, String username, String ordername) {
    var index = client.index(ordersIndex);
    var search = SearchRequest.builder();
    var filterList = new ArrayList<>();
    if (dateFrom != null) {
      filterList.add("orderDate >= \"%s\"".formatted(dateFrom.format(DateTimeFormatter.ISO_DATE)));
    }
    if (dateTo != null) {
      filterList.add("orderDate <= \"%s\"".formatted(dateTo.format(DateTimeFormatter.ISO_DATE)));
    }
    if (username != null && !username.isEmpty()) {
      filterList.add("username = \"%s\"".formatted(username));
    }
    if (ordername != null && !ordername.isEmpty()) {
      filterList.add("orderName = \"%s\"".formatted(ordername));
    }
    search.filter(filterList.toArray(new String[] {}));
    log.info("{}", index.search(search.build()).getHits());
    return index.search(search.build()).getHits().stream()
        .map(values -> objectMapper.convertValue(values, OrderMessage.class))
        .collect(toList());
  }
}
