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
@RequestMapping("/api/projects")
public class ProjectHighlightController {
  private final ProjectHighlightRepository repository;

  public ProjectHighlightController(ProjectHighlightRepository repository) {
    this.repository = repository;
  }

  @GetMapping
  public List<ProjectHighlight> all() {
    return repository.findAll();
  }

  @GetMapping("/active")
  public List<ProjectHighlight> active() {
    return repository.findByActiveTrueOrderByDisplayOrderAsc();
  }

  @PostMapping
  public ProjectHighlight create(@Valid @RequestBody ProjectHighlight project) {
    return repository.save(project);
  }

  @PutMapping("/{id}")
  public ResponseEntity<ProjectHighlight> update(@PathVariable String id, @Valid @RequestBody ProjectHighlight project) {
    if (!repository.existsById(id)) {
      return ResponseEntity.notFound().build();
    }
    project.id = id;
    return ResponseEntity.ok(repository.save(project));
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
