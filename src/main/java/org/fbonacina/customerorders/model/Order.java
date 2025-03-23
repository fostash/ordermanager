package org.fbonacina.customerorders.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;
import lombok.*;

@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "orders")
public class Order {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;
  private String description;

  // @ManyToOne
  // @JoinColumn(name = "userId")
  // private User user;

  @Column(name = "user_id")
  private Long userId;

  @Column(name = "creation_date", nullable = false)
  private LocalDate orderDate;

  @OneToMany(mappedBy = "order")
  private List<OrderItem> items;

  @Enumerated
  @Column(name = "order_status")
  private OrderStatus status;
}
