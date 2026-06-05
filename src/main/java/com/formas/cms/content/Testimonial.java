package com.formas.cms.content;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;

@Entity
public class Testimonial {
  @Id
  @NotBlank
  public String id;

  @NotBlank
  public String name;

  public String location;
  @Column(columnDefinition = "TEXT")
  public String text;

  @Column(columnDefinition = "TEXT")
  public String image;
  public boolean approved = true;
  public boolean active = true;
}
