package com.formas.cms.orders;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class OrderRequest {
  @NotBlank
  public String name;

  @Email
  @NotBlank
  public String email;

  @NotBlank
  public String phone;

  public String city;
  public String address;
  public String notes;
  public String paymentMethod;

  public Long amountCents;
  public Long subtotalCents;
  public Long taxCents;

  @Valid
  @NotEmpty
  public List<OrderLineRequest> items;
}
