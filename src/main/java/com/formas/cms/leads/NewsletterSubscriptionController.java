package com.formas.cms.leads;

import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/newsletter-subscriptions")
public class NewsletterSubscriptionController {
  private final NewsletterSubscriptionRepository repository;

  public NewsletterSubscriptionController(NewsletterSubscriptionRepository repository) {
    this.repository = repository;
  }

  @GetMapping
  public List<NewsletterSubscription> all() {
    return repository.findAll();
  }

  @PostMapping
  public NewsletterSubscription create(@Valid @RequestBody NewsletterSubscription subscription) {
    return repository.findByEmailIgnoreCase(subscription.email.trim())
        .map(existing -> {
          existing.active = true;
          return repository.save(existing);
        })
        .orElseGet(() -> {
          subscription.id = null;
          subscription.email = subscription.email.trim().toLowerCase();
          subscription.active = true;
          subscription.createdAt = Instant.now();
          return repository.save(subscription);
        });
  }
}
