package com.formas.cms.leads;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsletterSubscriptionRepository extends JpaRepository<NewsletterSubscription, Long> {
  Optional<NewsletterSubscription> findByEmailIgnoreCase(String email);
}
