package com.formas.cms.storage;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/technical-sheets")
public class TechnicalSheetController {
  private static final String TECHNICAL_SHEETS_FOLDER = "fichas-tecnicas";

  private final Path storageRoot;

  public TechnicalSheetController(StorageProperties storageProperties) {
    this.storageRoot = Path.of(storageProperties.getRoot()).toAbsolutePath().normalize();
  }

  @GetMapping("/{fileName:.+}")
  public ResponseEntity<Resource> show(@PathVariable String fileName) throws MalformedURLException {
    String safeFileName = fileName.replace("\\", "/").replaceAll(".*/", "").trim();
    if (safeFileName.isBlank() || !safeFileName.toLowerCase().endsWith(".pdf")) {
      return ResponseEntity.notFound().build();
    }

    Path file = storageRoot.resolve(TECHNICAL_SHEETS_FOLDER).resolve(safeFileName).normalize();
    if (!file.startsWith(storageRoot) || !Files.isRegularFile(file)) {
      return ResponseEntity.notFound().build();
    }

    Resource resource = new UrlResource(file.toUri());
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_PDF)
        .header(HttpHeaders.CONTENT_DISPOSITION,
            ContentDisposition.inline().filename(safeFileName).build().toString())
        .body(resource);
  }
}
