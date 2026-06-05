package com.formas.cms.content;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectHighlightRepository extends JpaRepository<ProjectHighlight, String> {
  List<ProjectHighlight> findByActiveTrueOrderByDisplayOrderAsc();
}
