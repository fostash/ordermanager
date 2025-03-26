package org.fbonacina.customerorders.pubsub;

import org.fbonacina.customerorders.model.OrderMessage;

public interface OrderPublisher {
  void publish(OrderMessage message);
}
