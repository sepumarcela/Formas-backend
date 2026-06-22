package com.formas.cms.orders;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import java.util.List;

public class PaymentRequest {
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

  @NotNull
  @Min(1)
  public Long amountCents;

  public Long subtotalCents;
  public Long taxCents;
  public String paymentMethod;

  @Valid
  @NotEmpty
  public List<PaymentItemRequest> items;
}
