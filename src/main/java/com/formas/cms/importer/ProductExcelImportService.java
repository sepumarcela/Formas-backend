package com.formas.cms.importer;

import com.formas.cms.catalog.Product;
import com.formas.cms.catalog.ProductRepository;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProductExcelImportService {
  private final ProductRepository productRepository;

  public ProductExcelImportService(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  public ImportSummary importProducts(MultipartFile file) throws IOException {
    if (isCsv(file)) {
      return importProductsFromCsv(file);
    }

    int processed = 0;
    int saved = 0;
    int skipped = 0;

    try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
      Sheet sheet = workbook.getSheetAt(0);
      int headerRowIndex = findHeaderRow(sheet);
      Map<String, Integer> columns = readColumns(sheet.getRow(headerRowIndex));

      for (int i = headerRowIndex + 1; i <= sheet.getLastRowNum(); i++) {
        Row row = sheet.getRow(i);
        if (row == null) {
          continue;
        }
        processed++;
        String id = firstText(row, columns, "id");
        String categoryId = firstText(row, columns, "categoria_id", "categoryId", "category_id", "categoria");
        String name = firstText(row, columns, "nombre", "name");
        if (id.isBlank() || categoryId.isBlank() || name.isBlank()) {
          skipped++;
          continue;
        }

        saveProduct(
            id,
            categoryId,
            name,
            firstText(row, columns, "precio_texto", "price", "precio"),
            money(row, columns, "precio_neto", "netPrice", "net_price"),
            firstText(row, columns, "medidas", "size"),
            firstText(row, columns, "descripcion", "description"),
            firstText(row, columns, "material"),
            firstText(row, columns, "color_acabado", "color", "colorFinish", "color_finish"),
            firstText(row, columns, "tiempo_entrega", "leadTime", "lead_time"),
            integer(row, columns, "descuento_porcentaje", "discountPercent", "discount_percent"),
            firstText(row, columns, "descuento_texto", "discountLabel", "discount_label"),
            date(row, columns, "descuento_inicio", "discountStart", "discount_start"),
            date(row, columns, "descuento_fin", "discountEnd", "discount_end"),
            bool(row, columns, false, "destacado", "featured"),
            bool(row, columns, true, "activo", "active"),
            firstText(row, columns, "image", "imagen"),
            firstText(row, columns, "ficha_tecnica", "technicalSheet", "technical_sheet", "ficha_pdf"));
        saved++;
      }
    }

    return new ImportSummary(processed, saved, skipped, "Importacion de productos finalizada.");
  }

  private ImportSummary importProductsFromCsv(MultipartFile file) throws IOException {
    List<List<String>> rows = readCsvRows(file);
    if (rows.isEmpty()) {
      return new ImportSummary(0, 0, 0, "El archivo no tiene filas para importar.");
    }

    int headerRowIndex = findCsvHeaderRow(rows);
    Map<String, Integer> columns = readCsvColumns(rows.get(headerRowIndex));
    int processed = 0;
    int saved = 0;
    int skipped = 0;

    for (int i = headerRowIndex + 1; i < rows.size(); i++) {
      List<String> row = rows.get(i);
      if (row.stream().allMatch(String::isBlank)) {
        continue;
      }
      processed++;
      String id = firstCsvText(row, columns, "id");
      String categoryId = firstCsvText(row, columns, "categoria_id", "categoryId", "category_id", "categoria");
      String name = firstCsvText(row, columns, "nombre", "name");
      if (id.isBlank() || categoryId.isBlank() || name.isBlank()) {
        skipped++;
        continue;
      }

      saveProduct(
          id,
          categoryId,
          name,
          firstCsvText(row, columns, "precio_texto", "price", "precio"),
          money(firstCsvText(row, columns, "precio_neto", "netPrice", "net_price")),
          firstCsvText(row, columns, "medidas", "size"),
          firstCsvText(row, columns, "descripcion", "description"),
          firstCsvText(row, columns, "material"),
          firstCsvText(row, columns, "color_acabado", "color", "colorFinish", "color_finish"),
          firstCsvText(row, columns, "tiempo_entrega", "leadTime", "lead_time"),
          integer(firstCsvText(row, columns, "descuento_porcentaje", "discountPercent", "discount_percent")),
          firstCsvText(row, columns, "descuento_texto", "discountLabel", "discount_label"),
          date(firstCsvText(row, columns, "descuento_inicio", "discountStart", "discount_start")),
          date(firstCsvText(row, columns, "descuento_fin", "discountEnd", "discount_end")),
          bool(firstCsvText(row, columns, "destacado", "featured"), false),
          bool(firstCsvText(row, columns, "activo", "active"), true),
          firstCsvText(row, columns, "image", "imagen"),
          firstCsvText(row, columns, "ficha_tecnica", "technicalSheet", "technical_sheet", "ficha_pdf"));
      saved++;
    }

    return new ImportSummary(processed, saved, skipped, "Importacion de productos finalizada.");
  }

  private void saveProduct(String id, String categoryId, String name, String priceText, BigDecimal netPrice,
      String size, String description, String material, String colorFinish, String leadTime,
      Integer discountPercent, String discountLabel, LocalDate discountStart, LocalDate discountEnd,
      boolean featured, boolean active, String image, String technicalSheet) {
    Product product = productRepository.findById(id).orElseGet(Product::new);
    product.id = id;
    product.categoryId = categoryId;
    product.name = name;
    product.priceText = priceText;
    product.netPrice = netPrice;
    product.size = size;
    product.description = description;
    product.material = material;
    product.colorFinish = colorFinish;
    product.leadTime = leadTime;
    product.discountPercent = discountPercent;
    product.discountLabel = discountLabel;
    product.discountStart = discountStart;
    product.discountEnd = discountEnd;
    product.featured = featured;
    product.active = active;
    product.image = image.isBlank() ? imagePath("productos", id) : image;
    if (!technicalSheet.isBlank()) {
      product.technicalSheet = technicalSheet;
    }

    productRepository.save(product);
  }

  private int findHeaderRow(Sheet sheet) {
    int maxRow = Math.min(sheet.getLastRowNum(), 20);
    for (int i = 0; i <= maxRow; i++) {
      Map<String, Integer> columns = readColumns(sheet.getRow(i));
      if (hasAnyColumn(columns, "id")
          && hasAnyColumn(columns, "nombre", "name")
          && hasAnyColumn(columns, "categoria_id", "categoryId", "category_id", "categoria")) {
        return i;
      }
    }
    return 0;
  }

  private int findCsvHeaderRow(List<List<String>> rows) {
    int maxRow = Math.min(rows.size(), 20);
    for (int i = 0; i < maxRow; i++) {
      Map<String, Integer> columns = readCsvColumns(rows.get(i));
      if (hasAnyColumn(columns, "id")
          && hasAnyColumn(columns, "nombre", "name")
          && hasAnyColumn(columns, "categoria_id", "categoryId", "category_id", "categoria")) {
        return i;
      }
    }
    return 0;
  }

  private boolean hasAnyColumn(Map<String, Integer> columns, String... keys) {
    for (String key : keys) {
      if (columns.containsKey(normalizeKey(key))) {
        return true;
      }
    }
    return false;
  }

  private Map<String, Integer> readColumns(Row header) {
    Map<String, Integer> columns = new HashMap<>();
    if (header == null) {
      return columns;
    }
    for (Cell cell : header) {
      String columnName = normalizeKey(cell.toString());
      if (!columnName.isBlank()) {
        columns.put(columnName, cell.getColumnIndex());
      }
    }
    return columns;
  }

  private Map<String, Integer> readCsvColumns(List<String> header) {
    Map<String, Integer> columns = new HashMap<>();
    for (int i = 0; i < header.size(); i++) {
      String columnName = normalizeKey(header.get(i));
      if (!columnName.isBlank()) {
        columns.put(columnName, i);
      }
    }
    return columns;
  }

  private String text(Row row, Map<String, Integer> columns, String key) {
    Integer index = columns.get(normalizeKey(key));
    if (index == null) {
      return "";
    }
    Cell cell = row.getCell(index);
    if (cell == null) {
      return "";
    }
    if (cell.getCellType() == CellType.NUMERIC) {
      double number = cell.getNumericCellValue();
      if (number == Math.rint(number)) {
        return String.valueOf((long) number);
      }
      return String.valueOf(number);
    }
    if (cell.getCellType() == CellType.BOOLEAN) {
      return String.valueOf(cell.getBooleanCellValue());
    }
    return cell.toString().trim();
  }

  private String firstText(Row row, Map<String, Integer> columns, String... keys) {
    for (String key : keys) {
      String value = text(row, columns, key);
      if (!value.isBlank()) {
        return value;
      }
    }
    return "";
  }

  private String firstCsvText(List<String> row, Map<String, Integer> columns, String... keys) {
    for (String key : keys) {
      Integer index = columns.get(normalizeKey(key));
      if (index != null && index < row.size()) {
        String value = row.get(index).trim();
        if (!value.isBlank()) {
          return value;
        }
      }
    }
    return "";
  }

  private BigDecimal money(Row row, Map<String, Integer> columns, String... keys) {
    return money(firstText(row, columns, keys));
  }

  private BigDecimal money(String text) {
    String value = text
        .replace("$", "")
        .replace(".", "")
        .replace(",", "")
        .trim();
    if (value.isBlank()) {
      return null;
    }
    return new BigDecimal(value);
  }

  private Integer integer(Row row, Map<String, Integer> columns, String... keys) {
    return integer(firstText(row, columns, keys));
  }

  private Integer integer(String text) {
    String value = text.trim();
    if (value.isBlank()) {
      return null;
    }
    return Integer.valueOf(value.replace("%", ""));
  }

  private LocalDate date(Row row, Map<String, Integer> columns, String... keys) {
    for (String key : keys) {
      Integer index = columns.get(normalizeKey(key));
      if (index == null) {
        continue;
      }
      Cell cell = row.getCell(index);
      if (cell == null) {
        continue;
      }
      if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
        return cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
      }
      String value = text(row, columns, key).trim();
      if (!value.isBlank()) {
        return LocalDate.parse(value);
      }
    }
    return null;
  }

  private LocalDate date(String text) {
    String value = text.trim();
    if (value.isBlank()) {
      return null;
    }
    return LocalDate.parse(value);
  }

  private boolean bool(Row row, Map<String, Integer> columns, boolean defaultValue, String... keys) {
    return bool(firstText(row, columns, keys), defaultValue);
  }

  private boolean bool(String text, boolean defaultValue) {
    String value = text.trim().toLowerCase();
    if (value.isBlank()) {
      return defaultValue;
    }
    return value.equals("true") || value.equals("verdadero") || value.equals("si") || value.equals("1");
  }

  private boolean isCsv(MultipartFile file) {
    String filename = file.getOriginalFilename();
    String contentType = file.getContentType();
    return (filename != null && filename.toLowerCase().endsWith(".csv"))
        || (contentType != null && contentType.toLowerCase().contains("csv"));
  }

  private List<List<String>> readCsvRows(MultipartFile file) throws IOException {
    List<String> records = new ArrayList<>();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
      String line;
      StringBuilder record = new StringBuilder();
      while ((line = reader.readLine()) != null) {
        if (record.length() > 0) {
          record.append('\n');
        }
        record.append(line);
        if (!hasOpenQuote(record.toString())) {
          records.add(record.toString());
          record.setLength(0);
        }
      }
      if (record.length() > 0) {
        records.add(record.toString());
      }
    }

    char delimiter = detectCsvDelimiter(records);
    List<List<String>> rows = new ArrayList<>();
    for (String record : records) {
      rows.add(parseCsvLine(record, delimiter));
    }
    return rows;
  }

  private char detectCsvDelimiter(List<String> records) {
    for (int i = 0; i < Math.min(records.size(), 20); i++) {
      List<String> semicolonColumns = parseCsvLine(records.get(i), ';');
      if (looksLikeProductHeader(semicolonColumns)) {
        return ';';
      }

      List<String> commaColumns = parseCsvLine(records.get(i), ',');
      if (looksLikeProductHeader(commaColumns)) {
        return ',';
      }
    }

    int semicolonScore = 0;
    int commaScore = 0;
    for (int i = 0; i < Math.min(records.size(), 10); i++) {
      semicolonScore += parseCsvLine(records.get(i), ';').size();
      commaScore += parseCsvLine(records.get(i), ',').size();
    }
    return semicolonScore > commaScore ? ';' : ',';
  }

  private boolean looksLikeProductHeader(List<String> row) {
    Map<String, Integer> columns = readCsvColumns(row);
    return hasAnyColumn(columns, "id")
        && hasAnyColumn(columns, "nombre", "name")
        && hasAnyColumn(columns, "categoria_id", "categoryId", "category_id", "categoria");
  }

  private boolean hasOpenQuote(String value) {
    boolean quoted = false;
    for (int i = 0; i < value.length(); i++) {
      if (value.charAt(i) == '"') {
        if (quoted && i + 1 < value.length() && value.charAt(i + 1) == '"') {
          i++;
        } else {
          quoted = !quoted;
        }
      }
    }
    return quoted;
  }

  private List<String> parseCsvLine(String line, char delimiter) {
    List<String> values = new ArrayList<>();
    StringBuilder current = new StringBuilder();
    boolean quoted = false;
    for (int i = 0; i < line.length(); i++) {
      char currentChar = line.charAt(i);
      if (currentChar == '"') {
        if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
          current.append('"');
          i++;
        } else {
          quoted = !quoted;
        }
      } else if (currentChar == delimiter && !quoted) {
        values.add(current.toString());
        current.setLength(0);
      } else {
        current.append(currentChar);
      }
    }
    values.add(current.toString());
    return values;
  }

  private String normalizeKey(String key) {
    return key == null ? "" : key.trim().replace("\uFEFF", "").toLowerCase().replace("_", "").replace(" ", "");
  }

  private String imagePath(String folder, String id) {
    return "/uploads/" + folder + "/" + id + ".jpg";
  }
}
