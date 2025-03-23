package org.fbonacina.customerorders.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.fbonacina.customerorders.model.Order;
import org.fbonacina.customerorders.model.OrderItem;
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

    var testProduct = Product.builder().id(1L).name("test product").stockQuantity(10).build();
    // var updatedProduct = Product.builder().id(1L).name("test product").stockQuantity(0).build();
    var testOrder = Optional.of(Order.builder().id(1L).name("test order").build());
    var testItem =
        OrderItem.builder().id(1L).product(testProduct).order(testOrder.get()).quantity(0L).build();
    when(productRepositoryMock.findById(anyLong())).thenReturn(Optional.of(testProduct));
    when(orderRepositoryMock.findById(anyLong())).thenReturn(testOrder);
    when(orderItemRepositoryMock.findByOrderIdAndProductId(anyLong(), anyLong()))
        .thenReturn(Optional.of(testItem));
    when(orderItemRepositoryMock.save(any(OrderItem.class))).thenReturn(testItem);

    var result = orderService.addProduct(1L, 1L, 1L, 10);

    verify(productRepositoryMock).findById(1L);
    verify(orderRepositoryMock).findById(1L);
    verify(productRepositoryMock).save(any(Product.class));

    assertThat(result.get().getQuantity()).isEqualTo(10L);
  }
}
