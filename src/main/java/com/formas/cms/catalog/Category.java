package com.formas.cms.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;

@Entity
public class Category {
  @Id
  @NotBlank
  public String id;

  @NotBlank
  public String name;

  @Column(columnDefinition = "TEXT")
  public String description;

  @Column(columnDefinition = "TEXT")
  public String heroImage;

  @Column(columnDefinition = "TEXT")
  public String image;
  public String icon = "shelf";
  public boolean active = true;
  public int displayOrder = 0;
}
