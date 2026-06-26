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
  private final LeadNotificationService notificationService;

  public ContactSubmissionController(ContactSubmissionRepository repository,
      LeadNotificationService notificationService) {
    this.repository = repository;
    this.notificationService = notificationService;
  }

  @GetMapping
  public List<ContactSubmission> all() {
    return repository.findAll();
  }

  @PostMapping
  public ContactSubmission create(@Valid @RequestBody ContactSubmission submission) {
    submission.id = null;
    submission.createdAt = java.time.Instant.now();
    ContactSubmission saved = repository.save(submission);
    notificationService.notifyContactSubmission(saved);
    return saved;
  }
}