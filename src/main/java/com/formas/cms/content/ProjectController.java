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
@RequestMapping("/api/project-gallery")
public class ProjectController {
  private final ProjectRepository repository;

  public ProjectController(ProjectRepository repository) {
    this.repository = repository;
  }

  @GetMapping
  public List<Project> all() {
    return repository.findAll();
  }

  @GetMapping("/active")
  public List<Project> active() {
    return repository.findByActiveTrueOrderByDisplayOrderAsc();
  }

  @PostMapping
  public Project create(@Valid @RequestBody Project project) {
    return repository.save(project);
  }

  @PutMapping("/{id}")
  public ResponseEntity<Project> update(@PathVariable String id, @Valid @RequestBody Project project) {
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
