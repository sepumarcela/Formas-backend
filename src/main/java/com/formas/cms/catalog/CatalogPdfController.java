package com.formas.cms.catalog;

import java.io.IOException;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalog")
public class CatalogPdfController {
  private final CatalogPdfService catalogPdfService;

  public CatalogPdfController(CatalogPdfService catalogPdfService) {
    this.catalogPdfService = catalogPdfService;
  }

  @GetMapping(value = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
  public ResponseEntity<byte[]> download() throws IOException {
    byte[] pdf = catalogPdfService.generate();
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_PDF)
        .header(HttpHeaders.CONTENT_DISPOSITION,
            ContentDisposition.attachment().filename("Catalogo FORMAS.pdf").build().toString())
        .body(pdf);
  }
}
