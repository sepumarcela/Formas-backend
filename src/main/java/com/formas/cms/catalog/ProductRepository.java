package com.formas.cms.catalog;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, String> {
  List<Product> findByActiveTrue();
  List<Product> findByCategoryIdAndActiveTrue(String categoryId);
  List<Product> findByFeaturedTrueAndActiveTrue();
}
