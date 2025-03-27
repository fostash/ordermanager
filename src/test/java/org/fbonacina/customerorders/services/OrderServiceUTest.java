package org.fbonacina.customerorders.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.fbonacina.customerorders.model.Order;
import org.fbonacina.customerorders.model.OrderItem;
import org.fbonacina.customerorders.model.Product;
import org.fbonacina.customerorders.model.User;
import org.fbonacina.customerorders.pubsub.RedisOrderPublisher;
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
    var redisOrderPublisher = mock(RedisOrderPublisher.class);
    var orderService =
        new OrderServiceImpl(
            orderRepositoryMock,
            productRepositoryMock,
            orderItemRepositoryMock,
            redisOrderPublisher);

    var testProduct = Product.builder().id(1L).name("test product").stockQuantity(10).build();
    var testItem =
        OrderItem.builder().id(1L).productId(testProduct.getId()).orderId(1L).quantity(0).build();
    var testOrder =
        Optional.of(
            Order.builder()
                .id(1L)
                .orderDate(LocalDate.now())
                .name("test order")
                .items(List.of(testItem))
                .user(User.builder().id(1L).build())
                .build());
    when(productRepositoryMock.findById(anyLong())).thenReturn(Optional.of(testProduct));
    when(orderRepositoryMock.findByIdAndUserId(anyLong(), anyLong())).thenReturn(testOrder);
    when(orderItemRepositoryMock.findByOrderIdAndProductId(anyLong(), anyLong()))
        .thenReturn(Optional.of(testItem));
    when(orderItemRepositoryMock.save(any(OrderItem.class))).thenReturn(testItem);
    doNothing().when(redisOrderPublisher).publish(any());

    var result = orderService.addProduct(1L, 1L, 1L, 10);

    verify(productRepositoryMock).findById(1L);
    verify(orderRepositoryMock).findByIdAndUserId(1L, 1L);
    verify(productRepositoryMock).save(any(Product.class));

    assertThat(result.get().getQuantity()).isEqualTo(10L);
  }
}
