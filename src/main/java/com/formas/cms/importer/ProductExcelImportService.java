package com.formas.cms.importer;

import com.formas.cms.catalog.Product;
import com.formas.cms.catalog.ProductRepository;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
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
        String id = text(row, columns, "id");
        String categoryId = text(row, columns, "categoria_id");
        String name = text(row, columns, "nombre");
        if (id.isBlank() || categoryId.isBlank() || name.isBlank()) {
          skipped++;
          continue;
        }

        Product product = productRepository.findById(id).orElseGet(Product::new);
        product.id = id;
        product.categoryId = categoryId;
        product.name = name;
        product.priceText = text(row, columns, "precio_texto");
        product.netPrice = money(row, columns, "precio_neto");
        product.size = text(row, columns, "medidas");
        product.description = text(row, columns, "descripcion");
        product.material = text(row, columns, "material");
        product.colorFinish = text(row, columns, "color_acabado");
        product.leadTime = text(row, columns, "tiempo_entrega");
        product.discountPercent = integer(row, columns, "descuento_porcentaje");
        product.discountLabel = text(row, columns, "descuento_texto");
        product.discountStart = date(row, columns, "descuento_inicio");
        product.discountEnd = date(row, columns, "descuento_fin");
        product.featured = bool(row, columns, "destacado", false);
        product.active = bool(row, columns, "activo", true);
        product.image = imagePath("productos", id);

        productRepository.save(product);
        saved++;
      }
    }

    return new ImportSummary(processed, saved, skipped, "Importación de productos finalizada.");
  }

  private Map<String, Integer> readColumns(Row header) {
    Map<String, Integer> columns = new HashMap<>();
    if (header == null) {
      return columns;
    }
    for (Cell cell : header) {
      columns.put(cell.getStringCellValue().trim().toLowerCase(), cell.getColumnIndex());
    }
    return columns;
  }

  private String text(Row row, Map<String, Integer> columns, String key) {
    Integer index = columns.get(key);
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

  private BigDecimal money(Row row, Map<String, Integer> columns, String key) {
    String value = text(row, columns, key).replace(".", "").replace("$", "").trim();
    if (value.isBlank()) {
      return null;
    }
    return new BigDecimal(value);
  }

  private Integer integer(Row row, Map<String, Integer> columns, String key) {
    String value = text(row, columns, key).trim();
    if (value.isBlank()) {
      return null;
    }
    return Integer.valueOf(value.replace("%", ""));
  }

  private LocalDate date(Row row, Map<String, Integer> columns, String key) {
    String value = text(row, columns, key).trim();
    if (value.isBlank()) {
      return null;
    }
    return LocalDate.parse(value);
  }

  private boolean bool(Row row, Map<String, Integer> columns, String key, boolean defaultValue) {
    String value = text(row, columns, key).trim().toLowerCase();
    if (value.isBlank()) {
      return defaultValue;
    }
    return value.equals("true") || value.equals("verdadero") || value.equals("si") || value.equals("sí") || value.equals("1");
  }

  private String imagePath(String folder, String id) {
    return "/uploads/" + folder + "/" + id + ".jpg";
  }
}
