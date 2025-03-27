package org.fbonacina.customerorders.pubsub;

import org.fbonacina.customerorders.messages.OrderMessage;

public interface OrderPublisher {
  void publish(OrderMessage message);
}
