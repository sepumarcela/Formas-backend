package com.formas.cms.orders;

import jakarta.persistence.Embeddable;

@Embeddable
public class OrderItem {
  public String productId;
  public String name;
  public String category;
  public String image;
  public Integer quantity;
  public Long unitAmountCents;
  public Long totalAmountCents;
}
