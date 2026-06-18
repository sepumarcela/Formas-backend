package com.formas.cms.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
public class Product {
  @Id
  @NotBlank
  public String id;

  @NotBlank
  public String categoryId;

  @NotBlank
  public String name;

  public String priceText;
  public BigDecimal netPrice;
  public String size;
  @Column(columnDefinition = "TEXT")
  public String description;
  public String material;
  public String colorFinish;
  public String leadTime;
  @Column(columnDefinition = "TEXT")
  public String image;
  @Column(columnDefinition = "TEXT")
  public String technicalSheet;
  public Integer discountPercent;
  public String discountLabel;
  public LocalDate discountStart;
  public LocalDate discountEnd;
  public boolean featured = false;
  public boolean active = true;
}
