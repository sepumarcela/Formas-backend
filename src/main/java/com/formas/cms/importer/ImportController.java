package com.formas.cms.importer;

import com.formas.cms.storage.ImageStorageService;
import java.io.IOException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/import")
public class ImportController {
  private final ProductExcelImportService productExcelImportService;
  private final ProductImageImportService productImageImportService;
  private final ImageStorageService imageStorageService;

  public ImportController(ProductExcelImportService productExcelImportService,
      ProductImageImportService productImageImportService,
      ImageStorageService imageStorageService) {
    this.productExcelImportService = productExcelImportService;
    this.productImageImportService = productImageImportService;
    this.imageStorageService = imageStorageService;
  }

  @PostMapping("/products/excel")
  public ImportSummary importProducts(@RequestParam MultipartFile file) throws IOException {
    return productExcelImportService.importProducts(file);
  }

  @PostMapping("/images/zip")
  public Map<String, Object> importImages(@RequestParam String folder, @RequestParam MultipartFile file) throws IOException {
    int saved = imageStorageService.storeZip(folder, file);
    return Map.of("saved", saved, "folder", folder);
  }

  @PostMapping("/products/images/zip")
  public ProductImageImportResult importProductImages(@RequestParam MultipartFile file) throws IOException {
    return productImageImportService.importProductImages(file);
  }

  @PostMapping("/images/file")
  public Map<String, Object> importImage(@RequestParam String folder, @RequestParam MultipartFile file) throws IOException {
    String url = imageStorageService.store(folder, file);
    return Map.of("url", url, "folder", folder);
  }

  @ExceptionHandler({ IllegalArgumentException.class, IllegalStateException.class, IOException.class })
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String, Object> handleImportError(Exception error) {
    return Map.of("message", error.getMessage());
  }
}
