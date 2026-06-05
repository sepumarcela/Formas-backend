package com.formas.cms.content;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/testimonials")
public class TestimonialController {
  private final TestimonialRepository repository;

  public TestimonialController(TestimonialRepository repository) {
    this.repository = repository;
  }

  @GetMapping
  public List<Testimonial> all() {
    return repository.findAll();
  }

  @GetMapping("/approved")
  public List<Testimonial> approved() {
    return repository.findByActiveTrueAndApprovedTrue();
  }

  @PostMapping
  public Testimonial create(@Valid @RequestBody Testimonial testimonial) {
    return repository.save(testimonial);
  }

  @PutMapping("/{id}")
  public ResponseEntity<Testimonial> update(@PathVariable String id, @Valid @RequestBody Testimonial testimonial) {
    if (!repository.existsById(id)) {
      return ResponseEntity.notFound().build();
    }
    testimonial.id = id;
    return ResponseEntity.ok(repository.save(testimonial));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable String id) {
    if (!repository.existsById(id)) {
      return ResponseEntity.notFound().build();
    }
    repository.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
