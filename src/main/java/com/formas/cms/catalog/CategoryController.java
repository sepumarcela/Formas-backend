package com.formas.cms.catalog;

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
@RequestMapping("/api/categories")
public class CategoryController {
  private final CategoryRepository repository;

  public CategoryController(CategoryRepository repository) {
    this.repository = repository;
  }

  @GetMapping
  public List<Category> all() {
    return repository.findAll();
  }

  @GetMapping("/{id}")
  public ResponseEntity<Category> one(@PathVariable String id) {
    return ResponseEntity.of(repository.findById(id));
  }

  @PostMapping
  public Category create(@Valid @RequestBody Category category) {
    return repository.save(category);
  }

  @PutMapping("/{id}")
  public ResponseEntity<Category> update(@PathVariable String id, @Valid @RequestBody Category category) {
    if (!repository.existsById(id)) {
      return ResponseEntity.notFound().build();
    }
    category.id = id;
    return ResponseEntity.ok(repository.save(category));
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
