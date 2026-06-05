package com.formas.cms.content;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HeroSlideRepository extends JpaRepository<HeroSlide, Long> {
  List<HeroSlide> findByActiveTrueOrderByDisplayOrderAsc();
}
