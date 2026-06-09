package com.formas.cms.leads;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

@Entity
public class ContactSubmission {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @NotBlank
  public String name;

  public String phone;

  @Email
  @NotBlank
  public String email;

  public String interest;

  @Column(columnDefinition = "TEXT")
  public String message;

  public Instant createdAt = Instant.now();
}
