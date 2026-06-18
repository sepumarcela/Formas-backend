package com.formas.cms.importer;

import com.formas.cms.catalog.Product;
import com.formas.cms.catalog.ProductRepository;
import com.formas.cms.storage.ImageStorageService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProductTechnicalSheetImportService {
  private static final String TECHNICAL_SHEETS_FOLDER = "fichas-tecnicas";

  private final ImageStorageService storageService;
  private final ProductRepository productRepository;

  public ProductTechnicalSheetImportService(ImageStorageService storageService, ProductRepository productRepository) {
    this.storageService = storageService;
    this.productRepository = productRepository;
  }

  public ProductImageImportResult importTechnicalSheets(MultipartFile file) throws IOException {
    Map<String, String> storedFiles = storageService.storePdfZipWithUrls(TECHNICAL_SHEETS_FOLDER, file);
    List<String> updatedProductIds = new ArrayList<>();
    List<String> unmatchedFiles = new ArrayList<>();

    for (Map.Entry<String, String> entry : storedFiles.entrySet()) {
      String fileName = entry.getKey();
      String productId = removePdfExtension(fileName);
      Product product = productRepository.findById(productId).orElse(null);

      if (product == null) {
        unmatchedFiles.add(fileName);
        continue;
      }

      product.technicalSheet = entry.getValue();
      productRepository.save(product);
      updatedProductIds.add(product.id);
    }

    return new ProductImageImportResult(
        storedFiles.size(),
        updatedProductIds.size(),
        unmatchedFiles.size(),
        updatedProductIds,
        unmatchedFiles);
  }

  private String removePdfExtension(String fileName) {
    String lower = fileName.toLowerCase(Locale.ROOT);
    return lower.endsWith(".pdf") ? fileName.substring(0, fileName.length() - 4) : fileName;
  }
}
