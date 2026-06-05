package com.formas.cms.content;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

@Entity
public class BlogPost {
  @Id
  @NotBlank
  public String id;

  @NotBlank
  public String title;

  public String category;
  public String displayDate;

  @Column(columnDefinition = "TEXT")
  public String excerpt;

  @Column(columnDefinition = "TEXT")
  public String content;

  @Column(columnDefinition = "TEXT")
  public String image;
  public LocalDate publishedAt;
  public boolean active = true;
}
