package org.fbonacina.customerorders.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.LocalDate;
import java.util.List;
import org.fbonacina.customerorders.messages.OrderMessage;

public interface MeilisearchService {

  void indexOrder(String message) throws JsonProcessingException;

  List<OrderMessage> search(
      LocalDate dateFrom, LocalDate dateTo, String username, String ordername);
}
