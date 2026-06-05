package com.formas.cms.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private final ObjectMapper objectMapper;
  private final String secret;
  private final long expirationMinutes;

  public JwtService(ObjectMapper objectMapper,
      @Value("${formas.jwt.secret}") String secret,
      @Value("${formas.jwt.expiration-minutes}") long expirationMinutes) {
    this.objectMapper = objectMapper;
    this.secret = secret;
    this.expirationMinutes = expirationMinutes;
  }

  public LoginResponse createToken(String email) {
    long expiresAt = Instant.now().plusSeconds(expirationMinutes * 60).getEpochSecond();
    String header = encodeJson(Map.of("alg", "HS256", "typ", "JWT"));
    String payload = encodeJson(Map.of("sub", email, "exp", expiresAt));
    String token = header + "." + payload + "." + sign(header + "." + payload);
    return new LoginResponse(token, email, expiresAt);
  }

  public String validate(String token) {
    String[] parts = token.split("\\.");
    if (parts.length != 3 || !MessageDigest.isEqual(sign(parts[0] + "." + parts[1]).getBytes(StandardCharsets.UTF_8), parts[2].getBytes(StandardCharsets.UTF_8))) {
      return null;
    }

    try {
      byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
      Map<String, Object> payload = objectMapper.readValue(payloadBytes, new TypeReference<>() {});
      String email = (String) payload.get("sub");
      Number expiresAt = (Number) payload.get("exp");
      if (email == null || expiresAt == null || expiresAt.longValue() < Instant.now().getEpochSecond()) {
        return null;
      }
      return email;
    } catch (Exception exception) {
      return null;
    }
  }

  private String encodeJson(Map<String, Object> value) {
    try {
      return Base64.getUrlEncoder().withoutPadding().encodeToString(objectMapper.writeValueAsBytes(value));
    } catch (Exception exception) {
      throw new IllegalStateException("No se pudo crear el token.", exception);
    }
  }

  private String sign(String value) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception exception) {
      throw new IllegalStateException("No se pudo firmar el token.", exception);
    }
  }
}
