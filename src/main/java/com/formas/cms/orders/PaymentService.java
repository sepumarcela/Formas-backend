package com.formas.cms.orders;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {
  private final OrderRepository repository;
  private final WompiProperties wompi;
  private final ObjectMapper objectMapper;

  public PaymentService(OrderRepository repository, WompiProperties wompi, ObjectMapper objectMapper) {
    this.repository = repository;
    this.wompi = wompi;
    this.objectMapper = objectMapper;
  }

  public PaymentResponse createPayment(PaymentRequest request) {
    validateAmount(request);

    Order order = buildOrder(request);
    order.paymentProvider = "WOMPI";
    order = repository.save(order);

    PaymentResponse response = new PaymentResponse();
    response.reference = order.reference;
    response.configured = wompi.isCheckoutConfigured();

    if (!response.configured) {
      response.message = "Wompi todavia no esta configurado. Agrega las llaves cuando termine el cambio de razon social.";
      return response;
    }

    String signature = sha256(order.reference + order.amountCents + order.currency + wompi.getIntegritySecret());
    Map<String, String> params = new LinkedHashMap<>();
    params.put("public-key", wompi.getPublicKey());
    params.put("currency", order.currency);
    params.put("amount-in-cents", String.valueOf(order.amountCents));
    params.put("reference", order.reference);
    params.put("signature:integrity", signature);
    params.put("customer-data:email", order.customer.email);
    params.put("customer-data:full-name", order.customer.name);
    params.put("customer-data:phone-number", order.customer.phone);

    if (wompi.getRedirectUrl() != null && !wompi.getRedirectUrl().isBlank()) {
      params.put("redirect-url", wompi.getRedirectUrl());
    }

    response.checkoutUrl = wompi.getCheckoutUrl();
    response.checkoutParams = params;
    return response;
  }

  public PaymentResponse createCoordinatedOrder(OrderRequest request) {
    Order order = buildOrder(request);
    order.paymentProvider = "COORDINAR_COMPRA";
    order = repository.save(order);

    PaymentResponse response = new PaymentResponse();
    response.reference = order.reference;
    response.configured = false;
    response.message = "Pedido registrado para coordinar compra.";
    return response;
  }

  public void handleWompiEvent(String payload) {
    try {
      JsonNode root = objectMapper.readTree(payload);
      JsonNode transaction = root.path("data").path("transaction");
      String reference = transaction.path("reference").asText("");
      if (reference.isBlank()) return;

      repository.findByReference(reference).ifPresent((order) -> {
        order.providerPayload = payload;
        order.providerTransactionId = transaction.path("id").asText(order.providerTransactionId);
        order.status = mapStatus(transaction.path("status").asText(""));
        order.updatedAt = Instant.now();
        repository.save(order);
      });
    } catch (Exception ignored) {
      // Wompi retries webhooks; keep this endpoint tolerant so a malformed event does not break checkout.
    }
  }

  private void validateAmount(PaymentRequest request) {
    long itemsTotal = request.items.stream()
        .mapToLong((item) -> item.unitAmountCents * item.quantity)
        .sum();

    if (itemsTotal != request.amountCents) {
      throw new IllegalArgumentException("El total del pedido no coincide con los productos.");
    }
  }

  private Order buildOrder(PaymentRequest request) {
    OrderRequest orderRequest = new OrderRequest();
    orderRequest.name = request.name;
    orderRequest.email = request.email;
    orderRequest.phone = request.phone;
    orderRequest.city = request.city;
    orderRequest.address = request.address;
    orderRequest.notes = request.notes;
    orderRequest.paymentMethod = request.paymentMethod;
    orderRequest.amountCents = request.amountCents;
    orderRequest.subtotalCents = request.subtotalCents;
    orderRequest.taxCents = request.taxCents;
    orderRequest.items = request.items.stream().map((item) -> {
      OrderLineRequest line = new OrderLineRequest();
      line.productId = item.productId;
      line.name = item.name;
      line.category = item.category;
      line.image = item.image;
      line.quantity = item.quantity;
      line.unitAmountCents = item.unitAmountCents;
      return line;
    }).toList();

    return buildOrder(orderRequest);
  }

  private Order buildOrder(OrderRequest request) {
    Order order = new Order();
    order.reference = "FORMAS-INT-" + Instant.now().toEpochMilli() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    order.amountCents = request.amountCents;
    order.subtotalCents = request.subtotalCents;
    order.taxCents = request.taxCents;
    order.paymentMethod = request.paymentMethod;
    order.customer.name = request.name;
    order.customer.email = request.email;
    order.customer.phone = request.phone;
    order.customer.city = request.city;
    order.customer.address = request.address;
    order.customer.notes = request.notes;

    for (OrderLineRequest item : request.items) {
      OrderItem orderItem = new OrderItem();
      orderItem.productId = item.productId;
      orderItem.name = item.name;
      orderItem.category = item.category;
      orderItem.image = item.image;
      orderItem.quantity = item.quantity;
      orderItem.unitAmountCents = item.unitAmountCents;
      orderItem.totalAmountCents = item.unitAmountCents == null ? null : item.unitAmountCents * item.quantity;
      order.items.add(orderItem);
    }

    return order;
  }

  private OrderStatus mapStatus(String status) {
    return switch (status) {
      case "APPROVED" -> OrderStatus.APPROVED;
      case "DECLINED" -> OrderStatus.DECLINED;
      case "ERROR" -> OrderStatus.ERROR;
      case "VOIDED" -> OrderStatus.CANCELLED;
      default -> OrderStatus.PENDING;
    };
  }

  private String sha256(String value) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
      StringBuilder output = new StringBuilder();
      for (byte item : hash) {
        output.append(String.format("%02x", item));
      }
      return output.toString();
    } catch (Exception error) {
      throw new IllegalStateException("No se pudo firmar el pago.", error);
    }
  }
}
