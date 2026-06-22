package com.formas.cms.orders;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customer_order")
public class Order {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Column(nullable = false, unique = true)
  public String reference;

  @Enumerated(EnumType.STRING)
  public OrderStatus status = OrderStatus.PENDING;

  @Enumerated(EnumType.STRING)
  public PaymentProvider paymentProvider = PaymentProvider.WOMPI;

  public String providerTransactionId;
  public String currency = "COP";
  public Long amountCents;
  public Long subtotalCents;
  public Long taxCents;
  public String paymentMethod;

  @Embedded
  public CustomerInfo customer = new CustomerInfo();

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "customer_order_item", joinColumns = @JoinColumn(name = "order_id"))
  public List<OrderItem> items = new ArrayList<>();

  @Column(columnDefinition = "TEXT")
  public String providerPayload;

  public Instant createdAt = Instant.now();
  public Instant updatedAt = Instant.now();
}
