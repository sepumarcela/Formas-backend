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
public class ProductImageImportService {
  private static final String PRODUCT_FOLDER = "productos";

  private final ImageStorageService imageStorageService;
  private final ProductRepository productRepository;

  public ProductImageImportService(ImageStorageService imageStorageService, ProductRepository productRepository) {
    this.imageStorageService = imageStorageService;
    this.productRepository = productRepository;
  }

  public ProductImageImportResult importProductImages(MultipartFile file) throws IOException {
    Map<String, String> storedImages = imageStorageService.storeZipWithUrls(PRODUCT_FOLDER, file);
    List<String> updatedProductIds = new ArrayList<>();
    List<String> unmatchedFiles = new ArrayList<>();

    for (Map.Entry<String, String> entry : storedImages.entrySet()) {
      String fileName = entry.getKey();
      String productId = removeExtension(fileName);
      Product product = productRepository.findById(productId).orElse(null);

      if (product == null) {
        unmatchedFiles.add(fileName);
        continue;
      }

      product.image = entry.getValue();
      productRepository.save(product);
      updatedProductIds.add(product.id);
    }

    return new ProductImageImportResult(
        storedImages.size(),
        updatedProductIds.size(),
        unmatchedFiles.size(),
        updatedProductIds,
        unmatchedFiles);
  }

  private String removeExtension(String fileName) {
    String lower = fileName.toLowerCase(Locale.ROOT);
    for (String extension : List.of(".jpeg", ".jpg", ".png", ".webp")) {
      if (lower.endsWith(extension)) {
        return fileName.substring(0, fileName.length() - extension.length());
      }
    }
    return fileName;
  }
}
