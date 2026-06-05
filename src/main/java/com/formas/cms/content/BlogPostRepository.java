package com.formas.cms.content;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogPostRepository extends JpaRepository<BlogPost, String> {
  List<BlogPost> findByActiveTrue();
}
