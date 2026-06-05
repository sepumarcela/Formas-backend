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
@RequestMapping("/api/pages")
public class PageContentController {
  private final PageContentRepository repository;

  public PageContentController(PageContentRepository repository) {
    this.repository = repository;
  }

  @GetMapping
  public List<PageContent> all() {
    return repository.findAll();
  }

  @GetMapping("/{pageKey}")
  public ResponseEntity<PageContent> one(@PathVariable String pageKey) {
    return ResponseEntity.of(repository.findById(pageKey));
  }

  @PostMapping
  public PageContent create(@Valid @RequestBody PageContent page) {
    return repository.save(page);
  }

  @PutMapping("/{pageKey}")
  public ResponseEntity<PageContent> update(@PathVariable String pageKey, @Valid @RequestBody PageContent page) {
    if (!repository.existsById(pageKey)) {
      return ResponseEntity.notFound().build();
    }
    page.pageKey = pageKey;
    return ResponseEntity.ok(repository.save(page));
  }

  @DeleteMapping("/{pageKey}")
  public ResponseEntity<Void> delete(@PathVariable String pageKey) {
    if (!repository.existsById(pageKey)) {
      return ResponseEntity.notFound().build();
    }
    repository.deleteById(pageKey);
    return ResponseEntity.noContent().build();
  }
}
