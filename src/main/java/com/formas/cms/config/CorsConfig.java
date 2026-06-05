package com.formas.cms.config;

import java.util.List;
import com.formas.cms.storage.StorageProperties;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
  private final List<String> allowedOrigins;
  private final Path storageRoot;

  public CorsConfig(@Value("${formas.cors.allowed-origins}") List<String> allowedOrigins,
      StorageProperties storageProperties) {
    this.allowedOrigins = allowedOrigins;
    this.storageRoot = Path.of(storageProperties.getRoot()).toAbsolutePath().normalize();
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/api/**")
        .allowedOrigins(allowedOrigins.toArray(String[]::new))
        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        .allowedHeaders("*");
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/uploads/**")
        .addResourceLocations(storageRoot.toUri().toString() + "/");
  }
}
