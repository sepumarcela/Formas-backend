package com.formas.cms.security;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final String adminEmail;
  private final String adminPassword;
  private final JwtService jwtService;

  public AuthController(@Value("${formas.admin.email}") String adminEmail,
      @Value("${formas.admin.password}") String adminPassword,
      JwtService jwtService) {
    this.adminEmail = adminEmail;
    this.adminPassword = adminPassword;
    this.jwtService = jwtService;
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    if (!adminEmail.equalsIgnoreCase(request.email().trim()) || !adminPassword.equals(request.password())) {
      return ResponseEntity.status(401).build();
    }
    return ResponseEntity.ok(jwtService.createToken(adminEmail));
  }
}
