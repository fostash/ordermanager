package org.fbonacina.customerorders.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.fbonacina.customerorders.dto.NewOrder;
import org.fbonacina.customerorders.exceptions.OrderException;
import org.fbonacina.customerorders.model.User;
import org.fbonacina.customerorders.repositories.OrderItemRepository;
import org.fbonacina.customerorders.repositories.OrderRepository;
import org.fbonacina.customerorders.repositories.ProductRepository;
import org.fbonacina.customerorders.repositories.UserRepository;
import org.fbonacina.customerorders.utils.BaseITTest;
import org.fbonacina.customerorders.utils.DataFixture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@ActiveProfiles("ittest")
class OrderServiceITTest implements BaseITTest, DataFixture {

  @Autowired private OrderRepository orderRepository;
  @Autowired private OrderItemRepository orderItemRepository;
  @Autowired private ProductRepository productRepository;
  @Autowired private UserRepository userRepository;

  @Autowired OrderServiceImpl orderService;

  @Test
  public void createOrder() {
    var userData = userRepository.save(createUser());

    var orderId =
        orderService.createOrder(
            NewOrder.builder().name("order test").description("order test").build(), userData);
    assertThat(orderId).isGreaterThan(0);

    assertThrows(
        OrderException.class,
        () ->
            orderService.createOrder(
                NewOrder.builder().name("order test").description("order test").build(),
                createUser()));

    var notExistigUser =
        User.builder().firstName("fake").lastName("fake").email("fake").id(999L).build();
    assertThrows(
        OrderException.class,
        () ->
            orderService.createOrder(
                NewOrder.builder().name("order test").description("order test").build(),
                notExistigUser));
  }

  @Test
  public void addProduct() {
    // create scenario
    var productData = productRepository.save(createProduct(10));

    var userData = userRepository.save(createUser());
    var orderData = orderRepository.save(createOrder(userData));

    // BL
    var productId = productData.getId();
    var orderId = orderData.getId();
    var quantityRequested = 5;

    var updateOrderItem =
        orderService.addProduct(userData.getId(), productId, orderId, quantityRequested);

    assertThat(updateOrderItem.isPresent()).isTrue();
    assertThat(updateOrderItem.get().getQuantity()).isEqualTo(5);

    var updateProduct = productRepository.findById(productId);
    assertThat(updateProduct.isPresent()).isTrue();
    assertThat(updateProduct.get().getStockQuantity()).isEqualTo(5);
  }
}
