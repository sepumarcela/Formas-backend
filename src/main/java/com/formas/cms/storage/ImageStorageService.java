package com.formas.cms.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageStorageService {
  private final Path root;
  private final RestTemplate restTemplate = new RestTemplate();
  private final String cloudName;
  private final String apiKey;
  private final String apiSecret;
  private final String cloudinaryRootFolder;

  public ImageStorageService(StorageProperties properties,
      @Value("${cloudinary.cloud-name:}") String cloudName,
      @Value("${cloudinary.api-key:}") String apiKey,
      @Value("${cloudinary.api-secret:}") String apiSecret,
      @Value("${cloudinary.folder:formas}") String cloudinaryRootFolder) {
    this.root = Path.of(properties.getRoot()).toAbsolutePath().normalize();
    this.cloudName = cloudName;
    this.apiKey = apiKey;
    this.apiSecret = apiSecret;
    this.cloudinaryRootFolder = cloudinaryRootFolder;
  }

  public String store(String folder, MultipartFile file) throws IOException {
    String fileName = cleanFileName(file.getOriginalFilename());
    if (!isImage(fileName)) {
      throw new IllegalArgumentException("El archivo debe ser una imagen JPG, PNG o WEBP.");
    }
    if (isCloudinaryEnabled()) {
      return uploadToCloudinary(folder, fileName, file.getBytes());
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
        if (isCloudinaryEnabled()) {
          savedFiles.put(fileName, uploadToCloudinary(folder, fileName, zip.readAllBytes()));
          continue;
        }
        Path target = safeTarget(folder, fileName);
        Files.createDirectories(target.getParent());
        Files.copy(zip, target, StandardCopyOption.REPLACE_EXISTING);
        savedFiles.put(fileName, "/uploads/" + folder + "/" + fileName);
      }
    }
    return savedFiles;
  }

  private boolean isCloudinaryEnabled() {
    return !cloudName.isBlank() && !apiKey.isBlank() && !apiSecret.isBlank();
  }

  private String uploadToCloudinary(String folder, String fileName, byte[] bytes) {
    String publicId = removeExtension(fileName);
    String targetFolder = cloudinaryRootFolder + "/" + folder;
    long timestamp = Instant.now().getEpochSecond();
    String signature = sign(Map.of(
        "folder", targetFolder,
        "overwrite", "true",
        "public_id", publicId,
        "timestamp", String.valueOf(timestamp)));

    MultipartBodyBuilder builder = new MultipartBodyBuilder();
    builder.part("file", new NamedByteArrayResource(bytes, fileName))
        .filename(fileName)
        .contentType(MediaType.APPLICATION_OCTET_STREAM);
    builder.part("api_key", apiKey);
    builder.part("timestamp", String.valueOf(timestamp));
    builder.part("folder", targetFolder);
    builder.part("public_id", publicId);
    builder.part("overwrite", "true");
    builder.part("signature", signature);

    String url = "https://api.cloudinary.com/v1_1/" + cloudName + "/image/upload";
    Map<?, ?> response = restTemplate.postForObject(url, builder.build(), Map.class);
    Object secureUrl = response == null ? null : response.get("secure_url");
    if (secureUrl == null) {
      throw new IllegalStateException("Cloudinary no devolvio una URL segura para la imagen.");
    }
    return secureUrl.toString();
  }

  private String sign(Map<String, String> params) {
    StringBuilder payload = new StringBuilder();
    params.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .forEach((entry) -> {
          if (!payload.isEmpty()) {
            payload.append("&");
          }
          payload.append(entry.getKey()).append("=").append(entry.getValue());
        });
    payload.append(apiSecret);

    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-1");
      byte[] hash = digest.digest(payload.toString().getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hash);
    } catch (NoSuchAlgorithmException error) {
      throw new IllegalStateException("No se pudo firmar la carga de Cloudinary.", error);
    }
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

  private String removeExtension(String fileName) {
    String lower = fileName.toLowerCase(Locale.ROOT);
    for (String extension : new String[] { ".jpeg", ".jpg", ".png", ".webp" }) {
      if (lower.endsWith(extension)) {
        return fileName.substring(0, fileName.length() - extension.length());
      }
    }
    return fileName;
  }

  private static final class NamedByteArrayResource extends ByteArrayResource {
    private final String fileName;

    private NamedByteArrayResource(byte[] byteArray, String fileName) {
      super(byteArray);
      this.fileName = fileName;
    }

    @Override
    public String getFilename() {
      return fileName;
    }
  }
}
