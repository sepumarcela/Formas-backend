package com.formas.cms.config;

import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseTextColumnMigration implements ApplicationRunner {
  private final JdbcTemplate jdbcTemplate;

  public DatabaseTextColumnMigration(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public void run(ApplicationArguments args) {
    String databaseProduct = jdbcTemplate.execute((ConnectionCallback<String>) connection ->
        connection.getMetaData().getDatabaseProductName().toLowerCase());
    if (databaseProduct == null || databaseProduct.contains("h2")) {
      return;
    }

    List<String> statements = List.of(
        "alter table if exists hero_slide alter column image type text",
        "alter table if exists hero_slide alter column subtitle type text",
        "alter table if exists hero_slide alter column title type text",
        "alter table if exists hero_slide alter column title_accent type text",
        "alter table if exists hero_slide alter column eyebrow type text",
        "alter table if exists hero_slide alter column primary_label type text",
        "alter table if exists hero_slide alter column primary_url type text",
        "alter table if exists hero_slide alter column secondary_label type text",
        "alter table if exists hero_slide alter column secondary_url type text",
        "alter table if exists category alter column image type text",
        "alter table if exists category alter column hero_image type text",
        "alter table if exists category alter column description type text",
        "alter table if exists category add column if not exists icon varchar(255)",
        "update category set icon = 'shelf' where icon is null or icon = ''",
        "alter table if exists product alter column image type text",
        "alter table if exists product add column if not exists technical_sheet text",
        "alter table if exists product alter column technical_sheet type text",
        "alter table if exists product alter column description type text",
        "alter table if exists product alter column price_text type text",
        "alter table if exists product alter column size type text",
        "alter table if exists product alter column material type text",
        "alter table if exists product alter column color_finish type text",
        "alter table if exists product alter column lead_time type text",
        "alter table if exists page_content alter column hero_image type text",
        "alter table if exists page_content alter column description type text",
        "alter table if exists page_content alter column content_json type text",
        "alter table if exists page_content alter column breadcrumb type text",
        "alter table if exists page_content alter column eyebrow type text",
        "alter table if exists page_content alter column title type text",
        "alter table if exists page_content alter column cta_label type text",
        "alter table if exists project alter column image type text",
        "alter table if exists project alter column title type text",
        "alter table if exists project alter column location type text",
        "alter table if exists project_highlight alter column before_image type text",
        "alter table if exists project_highlight alter column after_image type text",
        "alter table if exists project_highlight alter column description type text",
        "alter table if exists project_highlight alter column title type text",
        "alter table if exists testimonial alter column image type text",
        "alter table if exists testimonial alter column text type text",
        "alter table if exists testimonial alter column location type text",
        "alter table if exists blog_post alter column image type text",
        "alter table if exists blog_post alter column excerpt type text",
        "alter table if exists blog_post alter column content type text",
        "alter table if exists blog_post alter column title type text",
        "alter table if exists blog_post alter column display_date type text");

    statements.forEach(jdbcTemplate::execute);
  }
}
