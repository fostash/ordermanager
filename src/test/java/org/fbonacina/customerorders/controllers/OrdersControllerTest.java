package org.fbonacina.customerorders.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.fbonacina.customerorders.dto.AddProductDto;
import org.fbonacina.customerorders.dto.NewOrderDto;
import org.fbonacina.customerorders.model.Order;
import org.fbonacina.customerorders.model.OrderItem;
import org.fbonacina.customerorders.services.JwtService;
import org.fbonacina.customerorders.services.OrderService;
import org.fbonacina.customerorders.services.UserService;
import org.fbonacina.customerorders.utils.DataFixture;
import org.fbonacina.customerorders.utils.JwtBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
@ActiveProfiles("utest")
class OrdersControllerTest implements JwtBuilder, DataFixture {

  @Autowired private MockMvc mockMvc;

  @Autowired private JwtService jwtService;

  @MockitoBean private UserService userService;
  @MockitoBean private OrderService orderService;

  @Test
  void retrieveOrderTest() throws Exception {

    this.mockMvc.perform(get("/api/v1/orders/1")).andExpect(status().isUnauthorized());

    when(orderService.readOrderDetails(anyLong()))
        .thenReturn(Optional.of(Order.builder().id(1L).name("test").description("test").build()));

    this.mockMvc
        .perform(
            get("/api/v1/orders/1")
                .header("Authorization", "Bearer " + jwtService.generateToken("test", "USER")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name").value("test"))
        .andExpect(jsonPath("$.description").value("test"));
  }

  @Test
  void readUserOrderTest() throws Exception {

    this.mockMvc.perform(get("/api/v1/orders/user/1")).andExpect(status().isUnauthorized());

    var user = createUser();
    when(orderService.readUserOrders(anyLong()))
        .thenReturn(List.of(createOrder(user), createOrder(user), createOrder(user)));

    this.mockMvc
        .perform(
            get("/api/v1/orders/user/1")
                .header("Authorization", "Bearer " + jwtService.generateToken("test", "USER")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(3)))
        .andExpect(jsonPath("$[0].name", startsWith("order-")))
        .andExpect(jsonPath("$[0].description", startsWith("order-description-")));
  }

  @Test
  void readUserOrderTestFailure() throws Exception {
    when(orderService.readUserOrders(anyLong())).thenThrow(RuntimeException.class);

    this.mockMvc
        .perform(
            get("/api/v1/orders/user/1")
                .header("Authorization", "Bearer " + jwtService.generateToken("test", "USER")))
        .andExpect(status().isInternalServerError());
  }

  @Test
  void createOrderTest() throws Exception {

    var user = createUser();
    var orderData =
        NewOrderDto.builder().name("new-test-order").description("new-test-order-descr").build();
    when(userService.findById(anyLong())).thenReturn(Optional.of(user));
    when(orderService.createOrder(orderData, user)).thenReturn(1L);

    this.mockMvc
        .perform(
            put("/api/v1/orders/user/1")
                .header("Authorization", "Bearer " + jwtService.generateToken("test", "USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(orderData)))
        .andExpect(status().isCreated());
  }

  @Test
  void addProductToOrderTest() throws Exception {

    var addProduct = AddProductDto.builder().productId(1L).userId(1L).quantity(5);
    when(orderService.addProduct(anyLong(), anyLong(), anyLong(), anyInt()))
        .thenReturn(Optional.of(OrderItem.builder().build()));

    this.mockMvc
        .perform(
            post("/api/v1/orders/1/items")
                .header("Authorization", "Bearer " + jwtService.generateToken("test", "USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(addProduct)))
        .andExpect(status().isAccepted());

    verify(orderService).addProduct(1L, 1L, 1L, 5);
  }

  @Test
  public void searchForOrders() {}
}
