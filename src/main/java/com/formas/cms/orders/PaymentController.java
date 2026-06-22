package com.formas.cms.orders;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
  private final PaymentService paymentService;

  public PaymentController(PaymentService paymentService) {
    this.paymentService = paymentService;
  }

  @PostMapping("/wompi/checkout")
  public PaymentResponse createWompiCheckout(@Valid @RequestBody PaymentRequest request) {
    return paymentService.createPayment(request);
  }

  @PostMapping("/orders")
  public PaymentResponse createOrder(@Valid @RequestBody OrderRequest request) {
    return paymentService.createCoordinatedOrder(request);
  }

  @PostMapping("/wompi/events")
  public ResponseEntity<Void> wompiEvents(@RequestBody String payload) {
    paymentService.handleWompiEvent(payload);
    return ResponseEntity.ok().build();
  }

}
