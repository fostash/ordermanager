package org.fbonacina.customerorders.repositories;

import java.util.Optional;
import org.fbonacina.customerorders.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
  Optional<OrderItem> findByOrderIdAndProductId(Long orderId, Long productId);
}
