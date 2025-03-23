package org.fbonacina.customerorders.services;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.fbonacina.customerorders.model.Product;
import org.fbonacina.customerorders.repositories.OrderItemRepository;
import org.fbonacina.customerorders.repositories.OrderRepository;
import org.fbonacina.customerorders.repositories.ProductRepository;
import org.junit.jupiter.api.Test;

class OrderServiceUTest {

  @Test
  public void addProduct() {
    var productRepositoryMock = mock(ProductRepository.class);
    var orderRepositoryMock = mock(OrderRepository.class);
    var orderItemRepositoryMock = mock(OrderItemRepository.class);
    var orderService =
        new OrderServiceImpl(orderRepositoryMock, productRepositoryMock, orderItemRepositoryMock);

    var testProduct =
        Optional.ofNullable(
            Product.builder().id(1L).name("test product").stockQuantity(10).build());
    when(productRepositoryMock.findById(anyLong())).thenReturn(testProduct);
  }
}
