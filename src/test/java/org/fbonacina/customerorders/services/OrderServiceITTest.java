package org.fbonacina.customerorders.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.stream.IntStream;
import org.fbonacina.customerorders.dto.NewOrderDto;
import org.fbonacina.customerorders.exceptions.OrderException;
import org.fbonacina.customerorders.model.User;
import org.fbonacina.customerorders.repositories.OrderItemRepository;
import org.fbonacina.customerorders.repositories.OrderRepository;
import org.fbonacina.customerorders.repositories.ProductRepository;
import org.fbonacina.customerorders.repositories.UserRepository;
import org.fbonacina.customerorders.utils.BaseITTest;
import org.fbonacina.customerorders.utils.DataFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@ActiveProfiles("ittest")
@EnableRetry
class OrderServiceITTest implements BaseITTest, DataFixture {

  @Autowired private OrderRepository orderRepository;
  @Autowired private OrderItemRepository orderItemRepository;
  @Autowired private ProductRepository productRepository;
  @Autowired private UserRepository userRepository;

  @Autowired OrderService orderService;

  @BeforeEach
  public void beforeEach() {
    orderItemRepository.deleteAll();
    orderRepository.deleteAll();
    productRepository.deleteAll();
    userRepository.deleteAll();
  }

  @Test
  public void createOrder() {
    var userData = userRepository.save(createUser());

    var order =
        orderService.createOrder(
            NewOrderDto.builder().name("order test").description("order test").build(), userData);
    assertThat(order.getId()).isGreaterThan(0);

    assertThrows(
        OrderException.class,
        () ->
            orderService.createOrder(
                NewOrderDto.builder().name("order test").description("order test").build(),
                createUser()));

    var notExistigUser =
        User.builder().firstName("fake").lastName("fake").email("fake").id(999L).build();
    assertThrows(
        OrderException.class,
        () ->
            orderService.createOrder(
                NewOrderDto.builder().name("order test").description("order test").build(),
                notExistigUser));
  }

  @Test
  public void createOrderException() {
    var userData = userRepository.save(createUser());
    var orderNotValid =
        NewOrderDto.builder()
            .name(
                "order-with-name-too-long-afddsafdafdsafdsafdsahuklfdsauhkhfdjskafghjekfghejskagfhseajgfhejafghdsjafghdsajfgdhajfgdhjsagfhdsajfghdsja,fghdsaj,fghdsja,fghdsaj,fhgusladgfaejsgfhejsa,fgheja,fgehaj,fgfgdhjsagfhdsja,fghdsajfghsajfgsa,fghdsaj,gdajsfgds,agfhjas,ghfghds,agfd,jsagfdhdjas,")
            .description("order-description")
            .build();
    assertThrows(OrderException.class, () -> orderService.createOrder(orderNotValid, userData));
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

  @Test
  public void updateProductQuantity() {
    // create scenario
    var productData = productRepository.save(createProduct(10));

    var userData = userRepository.save(createUser());
    var orderData = orderRepository.save(createOrder(userData));
    orderItemRepository.save(createOrderItem(productData, orderData, 5));

    // BL
    var productId = productData.getId();
    var orderId = orderData.getId();
    var quantityRequested = 5;

    var updateOrderItem =
        orderService.addProduct(userData.getId(), productId, orderId, quantityRequested);

    assertThat(updateOrderItem.isPresent()).isTrue();
    assertThat(updateOrderItem.get().getQuantity()).isEqualTo(10);

    var updatedProduct = productRepository.findById(productId);
    assertThat(updatedProduct.isPresent()).isTrue();
    assertThat(updatedProduct.get().getStockQuantity()).isEqualTo(5);
  }

  @Test
  public void addProductQuantityNotEnough() {
    // create scenario
    var productData = productRepository.save(createProduct(10));

    var userData = userRepository.save(createUser());
    var orderData = orderRepository.save(createOrder(userData));

    // BL
    var productId = productData.getId();
    var orderId = orderData.getId();
    var quantityRequested = 15;

    var ex =
        assertThrows(
            RuntimeException.class,
            () -> orderService.addProduct(userData.getId(), productId, orderId, quantityRequested));

    assertEquals(ex.getMessage(), "product.quantity-not-enough");
  }

  @Test
  public void orderSearchTest() {
    var productData1 = productRepository.save(createProduct(10));

    var userData1 = userRepository.save(createUser());
    var userData2 = userRepository.save(createUser());

    IntStream.range(0, 4)
        .forEach(
            idx -> orderRepository.save(createOrder(userData1, LocalDate.of(2000, 12, 12), "old")));

    IntStream.range(0, 4)
        .forEach(
            idx -> {
              if (idx % 2 == 0) {
                var orderData =
                    orderRepository.save(createOrder(userData1, LocalDate.of(2005, 12, 12), "old"));
                orderItemRepository.save(createOrderItem(productData1, orderData, 2));
              } else {
                var orderData =
                    orderRepository.save(
                        createOrder(userData2, LocalDate.of(2010, 12, 12), "morerecent"));
                orderItemRepository.save(createOrderItem(productData1, orderData, 2));
              }
            });

    IntStream.range(0, 4)
        .forEach(
            idx -> {
              if (idx % 2 == 0) {
                var orderData =
                    orderRepository.save(createOrder(userData1, LocalDate.of(2005, 12, 12), "old"));
                orderItemRepository.save(createOrderItem(productData1, orderData, 2));
              } else {
                var orderData =
                    orderRepository.save(createOrder(userData2, LocalDate.now(), "actual"));
                orderItemRepository.save(createOrderItem(productData1, orderData, 2));
              }
            });

    var resSearchByOrderName = orderService.searchOrder(null, null, null, "old");
    assertThat(resSearchByOrderName.size()).isEqualTo(8);

    var resSearchDateFrom =
        orderService.searchOrder(LocalDate.now(), null, userData2.getUsername(), null);
    assertThat(resSearchDateFrom.size()).isEqualTo(2);

    var resSearchDateToActual = orderService.searchOrder(null, LocalDate.now(), null, "actual");
    assertThat(resSearchDateToActual.size()).isEqualTo(2);

    var resSearchDateFromAndName = orderService.searchOrder(LocalDate.now(), null, null, "old");
    assertThat(resSearchDateFromAndName.size()).isEqualTo(0);

    var resSearchDateFromTo =
        orderService.searchOrder(LocalDate.now(), LocalDate.now(), null, null);
    assertThat(resSearchDateFromTo.size()).isEqualTo(2);
  }
}
