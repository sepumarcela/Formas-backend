package com.formas.cms.orders;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PaymentItemRequest {
  @NotBlank
  public String productId;

  @NotBlank
  public String name;

  public String category;
  public String image;

  @NotNull
  @Min(1)
  public Integer quantity;

  @NotNull
  @Min(1)
  public Long unitAmountCents;
}
