package org.fbonacina.customerorders.repositories;

import java.util.List;
import org.fbonacina.customerorders.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository
    extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
  List<Order> findByUserId(Long id);
}
