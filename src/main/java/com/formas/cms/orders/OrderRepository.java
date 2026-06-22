package com.formas.cms.orders;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
  Optional<Order> findByReference(String reference);
}
