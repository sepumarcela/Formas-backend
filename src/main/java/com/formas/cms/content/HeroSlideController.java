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
@RequestMapping("/api/hero-slides")
public class HeroSlideController {
  private final HeroSlideRepository repository;

  public HeroSlideController(HeroSlideRepository repository) {
    this.repository = repository;
  }

  @GetMapping
  public List<HeroSlide> all() {
    return repository.findAll();
  }

  @GetMapping("/active")
  public List<HeroSlide> active() {
    return repository.findByActiveTrueOrderByDisplayOrderAsc();
  }

  @PostMapping
  public HeroSlide create(@Valid @RequestBody HeroSlide slide) {
    return repository.save(slide);
  }

  @PutMapping("/{id}")
  public ResponseEntity<HeroSlide> update(@PathVariable Long id, @Valid @RequestBody HeroSlide slide) {
    if (!repository.existsById(id)) {
      return ResponseEntity.notFound().build();
    }
    slide.id = id;
    return ResponseEntity.ok(repository.save(slide));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    if (!repository.existsById(id)) {
      return ResponseEntity.notFound().build();
    }
    repository.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
