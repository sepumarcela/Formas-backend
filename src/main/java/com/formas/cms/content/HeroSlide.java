package com.formas.cms.content;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class HeroSlide {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  public String title;
  public String eyebrow;
  public String titleAccent;
  @Column(columnDefinition = "TEXT")
  public String subtitle;

  @Column(columnDefinition = "TEXT")
  public String image;
  public String primaryLabel;
  public String primaryUrl;
  public String secondaryLabel;
  public String secondaryUrl;
  public boolean active = true;
  public int displayOrder = 0;
}
