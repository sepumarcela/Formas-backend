package com.formas.cms.leads;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contact-submissions")
public class ContactSubmissionController {
  private final ContactSubmissionRepository repository;

  public ContactSubmissionController(ContactSubmissionRepository repository) {
    this.repository = repository;
  }

  @GetMapping
  public List<ContactSubmission> all() {
    return repository.findAll();
  }

  @PostMapping
  public ContactSubmission create(@Valid @RequestBody ContactSubmission submission) {
    submission.id = null;
    submission.createdAt = java.time.Instant.now();
    return repository.save(submission);
  }
}
