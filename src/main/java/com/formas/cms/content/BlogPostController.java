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
@RequestMapping("/api/blog-posts")
public class BlogPostController {
  private final BlogPostRepository repository;

  public BlogPostController(BlogPostRepository repository) {
    this.repository = repository;
  }

  @GetMapping
  public List<BlogPost> all() {
    return repository.findAll();
  }

  @GetMapping("/active")
  public List<BlogPost> active() {
    return repository.findByActiveTrue();
  }

  @PostMapping
  public BlogPost create(@Valid @RequestBody BlogPost post) {
    return repository.save(post);
  }

  @PutMapping("/{id}")
  public ResponseEntity<BlogPost> update(@PathVariable String id, @Valid @RequestBody BlogPost post) {
    if (!repository.existsById(id)) {
      return ResponseEntity.notFound().build();
    }
    post.id = id;
    return ResponseEntity.ok(repository.save(post));
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
