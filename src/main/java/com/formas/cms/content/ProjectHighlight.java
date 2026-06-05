package com.formas.cms.content;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;

@Entity
public class ProjectHighlight {
  @Id
  @NotBlank
  public String id;

  public String category;
  @NotBlank
  public String title;
  @Column(columnDefinition = "TEXT")
  public String description;

  @Column(columnDefinition = "TEXT")
  public String beforeImage;

  @Column(columnDefinition = "TEXT")
  public String afterImage;
  public boolean active = true;
  public int displayOrder = 0;
}
