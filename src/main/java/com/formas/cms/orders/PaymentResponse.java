package com.formas.cms.orders;

import java.util.Map;

public class PaymentResponse {
  public String reference;
  public String checkoutUrl;
  public Map<String, String> checkoutParams;
  public boolean configured;
  public String message;
}
