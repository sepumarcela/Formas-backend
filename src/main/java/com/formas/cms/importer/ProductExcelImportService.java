package com.formas.cms.importer;

import com.formas.cms.catalog.Product;
import com.formas.cms.catalog.ProductRepository;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
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
    int processed = 0;
    int saved = 0;
    int skipped = 0;

    try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
      Sheet sheet = workbook.getSheetAt(0);
      Map<String, Integer> columns = readColumns(sheet.getRow(0));

      for (int i = 1; i <= sheet.getLastRowNum(); i++) {
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

        Product product = productRepository.findById(id).orElseGet(Product::new);
        product.id = id;
        product.categoryId = categoryId;
        product.name = name;
        product.priceText = firstText(row, columns, "precio_texto", "price", "precio");
        product.netPrice = money(row, columns, "precio_neto", "netPrice", "net_price");
        product.size = firstText(row, columns, "medidas", "size");
        product.description = firstText(row, columns, "descripcion", "description");
        product.material = firstText(row, columns, "material");
        product.colorFinish = firstText(row, columns, "color_acabado", "color", "colorFinish", "color_finish");
        product.leadTime = firstText(row, columns, "tiempo_entrega", "leadTime", "lead_time");
        product.discountPercent = integer(row, columns, "descuento_porcentaje", "discountPercent", "discount_percent");
        product.discountLabel = firstText(row, columns, "descuento_texto", "discountLabel", "discount_label");
        product.discountStart = date(row, columns, "descuento_inicio", "discountStart", "discount_start");
        product.discountEnd = date(row, columns, "descuento_fin", "discountEnd", "discount_end");
        product.featured = bool(row, columns, false, "destacado", "featured");
        product.active = bool(row, columns, true, "activo", "active");

        String image = firstText(row, columns, "image", "imagen");
        product.image = image.isBlank() ? imagePath("productos", id) : image;
        String technicalSheet = firstText(row, columns, "ficha_tecnica", "technicalSheet", "technical_sheet", "ficha_pdf");
        if (!technicalSheet.isBlank()) {
          product.technicalSheet = technicalSheet;
        }

        productRepository.save(product);
        saved++;
      }
    }

    return new ImportSummary(processed, saved, skipped, "Importacion de productos finalizada.");
  }

  private Map<String, Integer> readColumns(Row header) {
    Map<String, Integer> columns = new HashMap<>();
    if (header == null) {
      return columns;
    }
    for (Cell cell : header) {
      columns.put(normalizeKey(cell.getStringCellValue()), cell.getColumnIndex());
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

  private BigDecimal money(Row row, Map<String, Integer> columns, String... keys) {
    String value = firstText(row, columns, keys)
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
    String value = firstText(row, columns, keys).trim();
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

  private boolean bool(Row row, Map<String, Integer> columns, boolean defaultValue, String... keys) {
    String value = firstText(row, columns, keys).trim().toLowerCase();
    if (value.isBlank()) {
      return defaultValue;
    }
    return value.equals("true") || value.equals("verdadero") || value.equals("si") || value.equals("1");
  }

  private String normalizeKey(String key) {
    return key == null ? "" : key.trim().toLowerCase().replace("_", "").replace(" ", "");
  }

  private String imagePath(String folder, String id) {
    return "/uploads/" + folder + "/" + id + ".jpg";
  }
}
