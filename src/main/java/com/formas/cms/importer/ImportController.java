package com.formas.cms.importer;

import com.formas.cms.storage.ImageStorageService;
import java.io.IOException;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/import")
public class ImportController {
  private final ProductExcelImportService productExcelImportService;
  private final ImageStorageService imageStorageService;

  public ImportController(ProductExcelImportService productExcelImportService, ImageStorageService imageStorageService) {
    this.productExcelImportService = productExcelImportService;
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

  @PostMapping("/images/file")
  public Map<String, Object> importImage(@RequestParam String folder, @RequestParam MultipartFile file) throws IOException {
    String url = imageStorageService.store(folder, file);
    return Map.of("url", url, "folder", folder);
  }
}
