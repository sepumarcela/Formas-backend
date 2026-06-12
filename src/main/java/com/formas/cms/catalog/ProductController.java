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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {
  private final ProductRepository repository;

  public ProductController(ProductRepository repository) {
    this.repository = repository;
  }

  @GetMapping
  public List<Product> all(@RequestParam(required = false) String categoryId,
      @RequestParam(defaultValue = "false") boolean featured) {
    if (featured) {
      return repository.findByFeaturedTrueAndActiveTrue();
    }
    if (categoryId != null && !categoryId.isBlank()) {
      return repository.findByCategoryIdAndActiveTrue(categoryId);
    }
    return repository.findByActiveTrue();
  }

  @GetMapping("/{id}")
  public ResponseEntity<Product> one(@PathVariable String id) {
    return ResponseEntity.of(repository.findById(id));
  }

  @PostMapping
  public Product create(@Valid @RequestBody Product product) {
    return repository.save(product);
  }

  @PutMapping("/{id}")
  public ResponseEntity<Product> update(@PathVariable String id, @Valid @RequestBody Product product) {
    if (!repository.existsById(id)) {
      return ResponseEntity.notFound().build();
    }
    product.id = id;
    return ResponseEntity.ok(repository.save(product));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable String id) {
    Product product = repository.findById(id).orElse(null);
    if (product == null) {
      return ResponseEntity.notFound().build();
    }
    product.active = false;
    repository.save(product);
    return ResponseEntity.noContent().build();
  }
}
