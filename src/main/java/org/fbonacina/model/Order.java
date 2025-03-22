package org.fbonacina.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

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
    @JoinColumn(name = "userId")
    private User user;

    @Column(name = "creation_date", nullable = false)
    private LocalDate orderDate;

    @OneToMany(mappedBy = "order")
    private List<OrderItem> items;

    @Enumerated
    @Column(name = "order_status")
    private OrderStatus status;
}
