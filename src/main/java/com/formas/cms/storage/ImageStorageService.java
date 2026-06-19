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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
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
      return uploadToCloudinary(folder, fileName, file.getBytes(), "image");
    }
    return storeLocal(folder, fileName, file.getInputStream());
  }

  public String storePdf(String folder, MultipartFile file) throws IOException {
    String fileName = cleanFileName(file.getOriginalFilename());
    if (!isPdf(fileName)) {
      throw new IllegalArgumentException("El archivo debe ser un PDF.");
    }
    return storeLocal(folder, fileName, file.getInputStream());
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
          savedFiles.put(fileName, uploadToCloudinary(folder, fileName, zip.readAllBytes(), "image"));
          continue;
        }
        savedFiles.put(fileName, storeLocal(folder, fileName, zip));
      }
    }
    return savedFiles;
  }

  public Map<String, String> storePdfZipWithUrls(String folder, MultipartFile file) throws IOException {
    Map<String, String> savedFiles = new LinkedHashMap<>();
    try (ZipInputStream zip = new ZipInputStream(file.getInputStream())) {
      ZipEntry entry;
      while ((entry = zip.getNextEntry()) != null) {
        if (entry.isDirectory() || !isPdf(entry.getName())) {
          continue;
        }
        String fileName = cleanFileName(Path.of(entry.getName()).getFileName().toString());
        savedFiles.put(fileName, storeLocal(folder, fileName, zip));
      }
    }
    return savedFiles;
  }

  private boolean isCloudinaryEnabled() {
    return !cloudName.isBlank() && !apiKey.isBlank() && !apiSecret.isBlank();
  }

  private String uploadToCloudinary(String folder, String fileName, byte[] bytes, String resourceType) {
    String publicId = cleanCloudinaryId(removeExtension(fileName));
    String targetFolder = cleanCloudinaryFolder(folder);
    long timestamp = Instant.now().getEpochSecond();
    String signature = sign(Map.of(
        "folder", targetFolder,
        "overwrite", "true",
        "public_id", publicId,
        "timestamp", String.valueOf(timestamp)));

    HttpHeaders fileHeaders = new HttpHeaders();
    fileHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);

    MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
    form.add("file", new HttpEntity<>(new NamedByteArrayResource(bytes, fileName), fileHeaders));
    form.add("api_key", apiKey);
    form.add("timestamp", String.valueOf(timestamp));
    form.add("folder", targetFolder);
    form.add("public_id", publicId);
    form.add("overwrite", "true");
    form.add("signature", signature);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(form, headers);

    String url = "https://api.cloudinary.com/v1_1/" + cloudName + "/" + resourceType + "/upload";
    Map<?, ?> response;
    try {
      response = restTemplate.postForObject(url, request, Map.class);
    } catch (RestClientResponseException error) {
      throw new IllegalStateException("Cloudinary rechazo el archivo: " + error.getResponseBodyAsString(), error);
    } catch (RestClientException error) {
      throw new IllegalStateException("No se pudo conectar con Cloudinary para subir el archivo.", error);
    }
    Object secureUrl = response == null ? null : response.get("secure_url");
    if (secureUrl == null) {
      throw new IllegalStateException("Cloudinary no devolvio una URL segura para el archivo.");
    }
    return secureUrl.toString();
  }

  private String storeLocal(String folder, String fileName, InputStream input) throws IOException {
    Path target = safeTarget(folder, fileName);
    Files.createDirectories(target.getParent());
    Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
    return "/uploads/" + folder + "/" + fileName;
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

  private boolean isPdf(String name) {
    return name.toLowerCase(Locale.ROOT).endsWith(".pdf");
  }

  private String cleanFileName(String name) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("El archivo no tiene nombre.");
    }
    return name.replace("\\", "/").replaceAll(".*/", "").trim();
  }

  private String removeExtension(String fileName) {
    String lower = fileName.toLowerCase(Locale.ROOT);
    for (String extension : new String[] { ".jpeg", ".jpg", ".png", ".webp", ".pdf" }) {
      if (lower.endsWith(extension)) {
        return fileName.substring(0, fileName.length() - extension.length());
      }
    }
    return fileName;
  }

  private String cleanCloudinaryFolder(String folder) {
    String safeFolder = cleanCloudinaryId(folder);
    String safeRoot = cleanCloudinaryId(cloudinaryRootFolder);
    return safeRoot.isBlank() ? safeFolder : safeRoot + "/" + safeFolder;
  }

  private String cleanCloudinaryId(String value) {
    if (value == null || value.isBlank()) {
      return "formas";
    }
    return value
        .toLowerCase(Locale.ROOT)
        .replaceAll("[^a-z0-9_-]+", "-")
        .replaceAll("^-+|-+$", "");
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
