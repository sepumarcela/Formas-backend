package com.formas.cms.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageStorageService {
  private final Path root;

  public ImageStorageService(StorageProperties properties) {
    this.root = Path.of(properties.getRoot()).toAbsolutePath().normalize();
  }

  public String store(String folder, MultipartFile file) throws IOException {
    String fileName = cleanFileName(file.getOriginalFilename());
    if (!isImage(fileName)) {
      throw new IllegalArgumentException("El archivo debe ser una imagen JPG, PNG o WEBP.");
    }
    Path target = safeTarget(folder, fileName);
    Files.createDirectories(target.getParent());
    try (InputStream input = file.getInputStream()) {
      Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
    }
    return "/uploads/" + folder + "/" + fileName;
  }

  public int storeZip(String folder, MultipartFile file) throws IOException {
    return storeZipWithUrls(folder, file).size();
  }

  public Map<String, String> storeZipWithUrls(String folder, MultipartFile file) throws IOException {
    Map<String, String> savedFiles = new LinkedHashMap<>();
    try (ZipInputStream zip = new ZipInputStream(file.getInputStream())) {
      ZipEntry entry;
      while ((entry = zip.getNextEntry()) != null) {
        if (entry.isDirectory() || !isImage(entry.getName())) {
          continue;
        }
        String fileName = cleanFileName(Path.of(entry.getName()).getFileName().toString());
        Path target = safeTarget(folder, fileName);
        Files.createDirectories(target.getParent());
        Files.copy(zip, target, StandardCopyOption.REPLACE_EXISTING);
        savedFiles.put(fileName, "/uploads/" + folder + "/" + fileName);
      }
    }
    return savedFiles;
  }

  private Path safeTarget(String folder, String fileName) {
    Path target = root.resolve(folder).resolve(fileName).normalize();
    if (!target.startsWith(root)) {
      throw new IllegalArgumentException("Ruta de archivo no permitida.");
    }
    return target;
  }

  private boolean isImage(String name) {
    String lower = name.toLowerCase(Locale.ROOT);
    return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".webp");
  }

  private String cleanFileName(String name) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("El archivo no tiene nombre.");
    }
    return name.replace("\\", "/").replaceAll(".*/", "").trim();
  }
}
