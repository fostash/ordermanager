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

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  @Column(name = "order_date", nullable = false)
  private LocalDate orderDate;

  @OneToMany
  @JoinColumn(name = "order_id")
  private List<OrderItem> items;
}
