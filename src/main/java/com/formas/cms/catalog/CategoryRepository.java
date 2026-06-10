package com.formas.cms.catalog;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, String> {
  List<Category> findByActiveTrueOrderByDisplayOrderAsc();
}
