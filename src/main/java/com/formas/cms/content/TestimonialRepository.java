package com.formas.cms.content;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestimonialRepository extends JpaRepository<Testimonial, String> {
  List<Testimonial> findByActiveTrueAndApprovedTrue();
}
