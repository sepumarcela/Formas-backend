package com.formas.cms.content;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;

@Entity
public class Project {
  @Id
  @NotBlank
  public String id;

  public String categoryKey;
  public String label;

  @NotBlank
  public String title;

  public String location;

  @Column(columnDefinition = "TEXT")
  public String image;

  public boolean active = true;
  public int displayOrder = 0;
}
