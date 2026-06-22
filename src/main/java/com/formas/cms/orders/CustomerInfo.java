package com.formas.cms.orders;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class CustomerInfo {
  public String name;
  public String email;
  public String phone;
  public String city;
  public String address;

  @Column(columnDefinition = "TEXT")
  public String notes;
}
