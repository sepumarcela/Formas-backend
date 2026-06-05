package com.formas.cms.content;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;

@Entity
public class PageContent {
  @Id
  @NotBlank
  public String pageKey;

  public String breadcrumb;
  public String eyebrow;
  public String title;
  @Column(columnDefinition = "TEXT")
  public String description;

  @Column(columnDefinition = "TEXT")
  public String heroImage;
  public String ctaLabel;

  @Column(columnDefinition = "TEXT")
  public String contentJson;

  public boolean active = true;
}
