package com.formas.cms;

import com.formas.cms.storage.StorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class FormasCmsApplication {
  public static void main(String[] args) {
    SpringApplication.run(FormasCmsApplication.class, args);
  }
}
