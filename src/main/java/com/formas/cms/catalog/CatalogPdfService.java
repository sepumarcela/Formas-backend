package com.formas.cms.catalog;

import com.formas.cms.storage.StorageProperties;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import org.springframework.stereotype.Service;

@Service
public class CatalogPdfService {
  private static final Color INK = new Color(58, 51, 45);
  private static final Color MUTED = new Color(116, 103, 92);
  private static final Color PAPER = new Color(247, 244, 239);
  private static final Color WARM = new Color(234, 228, 218);
  private static final Color LINE = new Color(216, 206, 193);
  private static final Color GOLD = new Color(168, 143, 116);
  private static final Color DARK = new Color(40, 34, 29);
  private static final int MAX_PDF_IMAGE_DIMENSION = 900;
  private static final long MAX_DIRECT_IMAGE_BYTES = 2L * 1024L * 1024L;
  private static final long MAX_DATA_IMAGE_BYTES = 14L * 1024L * 1024L;
  private static final float PDF_IMAGE_QUALITY = 0.68f;
  private static final List<String> CATEGORY_ORDER = List.of(
      "centros-entretenimiento",
      "closets",
      "cocinas",
      "muebles-bano",
      "bibliotecas",
      "centros-estudio",
      "repisas",
      "alcobas-infantiles");

  private final CategoryRepository categoryRepository;
  private final ProductRepository productRepository;
  private final Path storageRoot;

  public CatalogPdfService(CategoryRepository categoryRepository, ProductRepository productRepository,
      StorageProperties storageProperties) {
    this.categoryRepository = categoryRepository;
    this.productRepository = productRepository;
    this.storageRoot = Path.of(storageProperties.getRoot()).toAbsolutePath().normalize();
  }

  public byte[] generate() throws IOException {
    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      generate(out);
      return out.toByteArray();
    }
  }

  public void generate(OutputStream out) throws IOException {
    List<Category> categories = orderedCategories(categoryRepository.findByActiveTrueOrderByDisplayOrderAsc());
    List<Product> products = productRepository.findByActiveTrue();
    Map<String, Integer> pages = categoryStartPages(categories, products);

    try {
      Document document = new Document(PageSize.A4, 42, 42, 42, 42);
      PdfWriter.getInstance(document, out);
      document.open();
      document.addAuthor("FORMAS");
      document.addTitle("Catálogo FORMAS");

      cover(document, categories, products);
      visualIndex(document, categories, products, pages);
      philosophy(document);
      materials(document);
      process(document);

      for (int index = 0; index < categories.size(); index++) {
        Category category = categories.get(index);
        List<Product> categoryProducts = productsFor(category, products);
        categoryCover(document, category, categoryProducts, index + 1);
        productPages(document, category, categoryProducts);
      }

      contact(document);
      document.close();
    } catch (DocumentException error) {
      throw new IOException("No se pudo generar el catálogo PDF.", error);
    }
  }

  private List<Category> orderedCategories(List<Category> categories) {
    return categories.stream()
        .sorted(Comparator.comparingInt((Category category) -> {
          int index = CATEGORY_ORDER.indexOf(category.id);
          return index >= 0 ? index : 999;
        }).thenComparingInt(category -> category.displayOrder))
        .toList();
  }

  private Map<String, Integer> categoryStartPages(List<Category> categories, List<Product> products) {
    java.util.LinkedHashMap<String, Integer> pages = new java.util.LinkedHashMap<>();
    int page = 7;
    for (Category category : categories) {
      List<Product> items = productsFor(category, products);
      pages.put(category.id, page);
      page += 1 + Math.max(1, (int) Math.ceil(items.size() / 4.0));
    }
    return pages;
  }

  private List<Product> productsFor(Category category, List<Product> products) {
    return products.stream()
        .filter(product -> category.id.equals(product.categoryId))
        .filter(product -> product.active)
        .toList();
  }

  private void cover(Document document, List<Category> categories, List<Product> products) throws DocumentException {
    document.newPage();
    PdfPTable layout = fullPageTable(1);
    PdfPCell cell = cell(DARK, 28);
    cell.setFixedHeight(750);
    Image image = image(firstImage(categories, products));
    if (image != null) {
      image.scaleToFit(470, 285);
      image.setAlignment(Element.ALIGN_CENTER);
      cell.addElement(image);
      cell.addElement(space(70));
    } else {
      cell.addElement(space(230));
    }
    cell.addElement(kicker("CATÁLOGO DE PRODUCTOS", GOLD));
    cell.addElement(title("Espacios diseñados a la medida de tu vida.", white(), 54, 0.95f));
    cell.addElement(body("Una mirada editorial a las líneas, materiales y productos que FORMAS desarrolla para convertir mobiliario en arquitectura interior.", new Color(238, 231, 221), 14, 1.6f));
    cell.addElement(space(20));
    cell.addElement(body(LocalDate.now().format(DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", new Locale("es", "CO"))), new Color(210, 197, 180), 9, 1.2f));
    layout.addCell(cell);
    document.add(layout);
  }

  private void visualIndex(Document document, List<Category> categories, List<Product> products, Map<String, Integer> pages)
      throws DocumentException {
    List<List<Category>> chunks = chunks(categories, 4);
    for (int index = 0; index < chunks.size(); index++) {
      document.newPage();
      document.add(kickerParagraph("ÍNDICE VISUAL " + (index + 1) + " / " + chunks.size()));
      document.add(titleParagraph(index == 0 ? "Explora nuestras líneas" : "Más espacios para imaginar", 40));
      document.add(bodyParagraph("Una galería breve para recorrer las categorías del catálogo con una mirada más visual y editorial.", 12));
      document.add(space(16));

      PdfPTable grid = new PdfPTable(2);
      grid.setWidthPercentage(100);
      grid.setWidths(new float[] {1, 1});
      grid.getDefaultCell().setBorder(Rectangle.NO_BORDER);
      for (Category category : chunks.get(index)) {
        grid.addCell(indexCard(category, productsFor(category, products), pages.getOrDefault(category.id, 1)));
      }
      while (grid.size() % 4 != 0) {
        grid.addCell(blankCell());
      }
      document.add(grid);
    }
  }

  private PdfPCell indexCard(Category category, List<Product> products, int pageNumber) {
    PdfPCell card = cell(new Color(255, 253, 249), 0);
    card.setPadding(0);
    card.setBorderColor(LINE);
    card.setFixedHeight(285);

    Image image = image(category.image != null && !category.image.isBlank() ? category.image : firstProductImage(products));
    PdfPCell imageCell = cell(WARM, 0);
    imageCell.setFixedHeight(198);
    if (image != null) {
      image.scaleToFit(235, 185);
      image.setAlignment(Element.ALIGN_CENTER);
      imageCell.addElement(image);
    } else {
      imageCell.addElement(centered("Foto pendiente", MUTED, 10));
    }
    PdfPTable inner = new PdfPTable(1);
    inner.setWidthPercentage(100);
    inner.addCell(imageCell);

    PdfPCell text = cell(new Color(255, 253, 249), 14);
    text.addElement(title(category.name, INK, 25, 1.0f));
    text.addElement(kicker("PÁGINA " + pageNumber, GOLD));
    inner.addCell(text);
    card.addElement(inner);
    return card;
  }

  private void philosophy(Document document) throws DocumentException {
    document.newPage();
    centeredSectionHeader(document, "NUESTRA FILOSOFÍA",
        "No fabricamos muebles aislados. Diseñamos espacios para vivir mejor.");
    verticalItemList(document, List.of(
        List.of("Diseño a medida", "Cada proyecto nace de una necesidad real y se adapta al espacio, al uso y al estilo de vida."),
        List.of("Fabricación precisa", "Cuidamos proporciones, acabados y detalles técnicos para que el mueble se sienta integrado."),
        List.of("Acompañamiento", "Guiamos decisiones de material, color y distribución para construir confianza desde el primer contacto.")));
  }

  private void materials(Document document) throws DocumentException {
    document.newPage();
    document.add(kickerParagraph("DETALLES QUE ELEVAN EL RESULTADO"));
    document.add(titleParagraph("Materialidad cálida, funcionalidad precisa y acabados que se sienten bien.", 40));
    document.add(space(26));
    verticalNumberList(document, List.of(
        List.of("Materiales y acabados", "MDF RH, melamínicos, laminados, tonos madera y superficies fáciles de mantener."),
        List.of("Herrajes premium", "Sistemas funcionales para apertura, cierre, organización y uso diario con mayor comodidad."),
        List.of("Proyectos personalizados", "Medidas, distribuciones y detalles pensados para cocinas, closets, estudios, baños y zonas sociales.")));
  }

  private void process(Document document) throws DocumentException {
    document.newPage();
    centeredSectionHeader(document, "PROCESO DE FABRICACIÓN",
        "Un recorrido claro desde la idea hasta el espacio instalado.");
    verticalNumberList(document, List.of(
        List.of("Diagnóstico", "Entendemos el espacio, las medidas y la forma en que lo quieres usar."),
        List.of("Diseño", "Definimos distribución, materiales, acabados y detalles de fabricación."),
        List.of("Producción", "Fabricamos con precisión para lograr un resultado limpio y durable."),
        List.of("Instalación", "Cerramos el proyecto cuidando ajustes, remates y experiencia final.")));
  }

  private void categoryCover(Document document, Category category, List<Product> products, int index) throws DocumentException {
    document.newPage();
    PdfPTable table = new PdfPTable(2);
    table.setWidthPercentage(100);
    table.setWidths(new float[] {1.05f, 0.95f});
    PdfPCell imageCell = cell(WARM, 0);
    imageCell.setFixedHeight(750);
    Image image = image(category.image != null && !category.image.isBlank() ? category.image : firstProductImage(products));
    if (image != null) {
      image.scaleToFit(250, 720);
      image.setAlignment(Element.ALIGN_CENTER);
      imageCell.addElement(space(230));
      imageCell.addElement(image);
    }
    table.addCell(imageCell);

    PdfPCell content = cell(PAPER, 22);
    content.setFixedHeight(750);
    content.addElement(space(205));
    content.addElement(kicker("LÍNEA " + String.format("%02d", index), GOLD));
    content.addElement(title(category.name, INK, 34, 1.02f));
    content.addElement(body(categoryText(category), MUTED, 11, 1.55f));
    content.addElement(space(22));
    content.addElement(fact("ENFOQUE", "Diseño a medida"));
    content.addElement(fact("LÍNEA", products.size() + " productos"));
    content.addElement(fact("USO", "Interiorismo funcional"));
    table.addCell(content);
    document.add(table);
  }

  private void productPages(Document document, Category category, List<Product> products) throws DocumentException {
    List<List<Product>> pages = chunks(products, 4);
    if (pages.isEmpty()) {
      pages = List.of(List.of());
    }
    for (int pageIndex = 0; pageIndex < pages.size(); pageIndex++) {
      document.newPage();
      document.add(kickerParagraph("PRODUCTOS " + (pageIndex + 1) + " / " + pages.size()));
      document.add(titleParagraph(category.name, 36));
      document.add(space(12));

      PdfPTable grid = new PdfPTable(2);
      grid.setWidthPercentage(100);
      grid.setWidths(new float[] {1, 1});
      for (Product product : pages.get(pageIndex)) {
        grid.addCell(productCard(product));
      }
      while (grid.size() % 4 != 0) {
        grid.addCell(blankCell());
      }
      document.add(grid);
    }
  }

  private PdfPCell productCard(Product product) {
    PdfPCell card = cell(new Color(255, 253, 249), 0);
    card.setPadding(0);
    card.setBorderColor(LINE);
    card.setFixedHeight(315);

    PdfPTable inner = new PdfPTable(1);
    inner.setWidthPercentage(100);
    PdfPCell imageCell = cell(WARM, 0);
    imageCell.setFixedHeight(170);
    Image image = image(product.image);
    if (image != null) {
      image.scaleToFit(235, 160);
      image.setAlignment(Element.ALIGN_CENTER);
      imageCell.addElement(image);
    } else {
      imageCell.addElement(centered("Foto pendiente", MUTED, 10));
    }
    inner.addCell(imageCell);

    PdfPCell text = cell(new Color(255, 253, 249), 12);
    text.addElement(title(product.name, INK, 20, 1.0f));
    text.addElement(body(subtitle(product), MUTED, 9, 1.3f));
    text.addElement(space(4));
    text.addElement(body(detailLine(product), MUTED, 8, 1.35f));
    inner.addCell(text);
    card.addElement(inner);
    return card;
  }

  private void contact(Document document) throws DocumentException {
    document.newPage();
    PdfPTable table = fullPageTable(1);
    PdfPCell cell = cell(DARK, 42);
    cell.setFixedHeight(750);
    cell.addElement(space(360));
    cell.addElement(kicker("CONTACTO Y COTIZACIÓN", GOLD));
    cell.addElement(title("Hablemos del espacio que quieres transformar.", white(), 46, 0.98f));
    cell.addElement(body("Cuéntanos qué necesitas, comparte medidas o referentes, y te acompañamos para convertir la idea en una solución fabricable, funcional y coherente con tu estilo.", new Color(238, 231, 221), 13, 1.6f));
    table.addCell(cell);
    document.add(table);
  }

  private void centeredSectionHeader(Document document, String kicker, String title) throws DocumentException {
    Paragraph small = kickerParagraph(kicker);
    small.setAlignment(Element.ALIGN_CENTER);
    document.add(small);
    Paragraph heading = titleParagraph(title, 40);
    heading.setAlignment(Element.ALIGN_CENTER);
    document.add(heading);
    document.add(space(30));
  }

  private void verticalItemList(Document document, List<List<String>> items) throws DocumentException {
    PdfPTable table = new PdfPTable(2);
    table.setWidthPercentage(78);
    table.setHorizontalAlignment(Element.ALIGN_CENTER);
    table.setWidths(new float[] {0.16f, 0.84f});
    for (List<String> item : items) {
      table.addCell(circleCell());
      table.addCell(textItem(item.get(0), item.get(1)));
    }
    document.add(table);
  }

  private void verticalNumberList(Document document, List<List<String>> items) throws DocumentException {
    PdfPTable table = new PdfPTable(2);
    table.setWidthPercentage(82);
    table.setHorizontalAlignment(Element.ALIGN_CENTER);
    table.setWidths(new float[] {0.16f, 0.84f});
    for (int index = 0; index < items.size(); index++) {
      PdfPCell number = cell(PAPER, 0);
      number.setBorder(Rectangle.TOP);
      number.setBorderColor(GOLD);
      number.setPaddingTop(14);
      number.addElement(kicker(String.format("%02d", index + 1), GOLD));
      table.addCell(number);
      table.addCell(textItem(items.get(index).get(0), items.get(index).get(1)));
    }
    document.add(table);
  }

  private PdfPCell textItem(String heading, String text) {
    PdfPCell cell = cell(PAPER, 0);
    cell.setBorder(Rectangle.TOP);
    cell.setBorderColor(GOLD);
    cell.setPaddingTop(14);
    cell.setPaddingBottom(14);
    cell.addElement(title(heading, INK, 22, 1.08f));
    cell.addElement(body(text, MUTED, 10, 1.5f));
    return cell;
  }

  private PdfPCell circleCell() {
    PdfPCell cell = cell(PAPER, 0);
    cell.setBorder(Rectangle.TOP);
    cell.setBorderColor(GOLD);
    cell.setPaddingTop(18);
    cell.addElement(new Chunk("●", font(28, GOLD, Font.NORMAL)));
    return cell;
  }

  private PdfPTable fullPageTable(int columns) {
    PdfPTable table = new PdfPTable(columns);
    table.setWidthPercentage(100);
    return table;
  }

  private PdfPTable fact(String label, String value) {
    PdfPTable table = new PdfPTable(2);
    table.setWidthPercentage(100);
    table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
    PdfPCell labelCell = noBorder(kicker(label, GOLD));
    labelCell.setBorder(Rectangle.TOP);
    labelCell.setBorderColor(LINE);
    labelCell.setPaddingTop(10);
    labelCell.setPaddingBottom(10);
    PdfPCell valueCell = noBorder(new Phrase(value, font(10, INK, Font.BOLD)));
    valueCell.setBorder(Rectangle.TOP);
    valueCell.setBorderColor(LINE);
    valueCell.setPaddingTop(10);
    valueCell.setPaddingBottom(10);
    table.addCell(labelCell);
    table.addCell(valueCell);
    return table;
  }

  private PdfPCell noBorder(Element element) {
    PdfPCell cell = new PdfPCell();
    cell.setBorder(Rectangle.NO_BORDER);
    cell.addElement(element);
    return cell;
  }

  private PdfPCell blankCell() {
    PdfPCell cell = cell(PAPER, 0);
    cell.setBorder(Rectangle.NO_BORDER);
    return cell;
  }

  private PdfPCell cell(Color background, float padding) {
    PdfPCell cell = new PdfPCell();
    cell.setBorder(Rectangle.NO_BORDER);
    cell.setBackgroundColor(background);
    cell.setPadding(padding);
    return cell;
  }

  private Paragraph titleParagraph(String text, int size) {
    return title(text, INK, size, 1.0f);
  }

  private Paragraph kickerParagraph(String text) {
    return kicker(text, GOLD);
  }

  private Paragraph bodyParagraph(String text, int size) {
    return body(text, MUTED, size, 1.45f);
  }

  private Paragraph title(String text, Color color, int size, float leading) {
    Paragraph paragraph = new Paragraph(text == null ? "" : text, font(size, color, Font.NORMAL));
    paragraph.setLeading(size * leading);
    paragraph.setSpacingAfter(8);
    return paragraph;
  }

  private Paragraph kicker(String text, Color color) {
    Paragraph paragraph = new Paragraph((text == null ? "" : text).toUpperCase(Locale.ROOT), font(8, color, Font.BOLD));
    paragraph.setSpacingAfter(8);
    return paragraph;
  }

  private Paragraph body(String text, Color color, int size, float leading) {
    Paragraph paragraph = new Paragraph(text == null ? "" : text, font(size, color, Font.NORMAL));
    paragraph.setLeading(size * leading);
    paragraph.setSpacingAfter(8);
    return paragraph;
  }

  private Paragraph centered(String text, Color color, int size) {
    Paragraph paragraph = body(text, color, size, 1.2f);
    paragraph.setAlignment(Element.ALIGN_CENTER);
    return paragraph;
  }

  private Paragraph space(float height) {
    Paragraph paragraph = new Paragraph(" ");
    paragraph.setLeading(height);
    return paragraph;
  }

  private Font font(int size, Color color, int style) {
    return new Font(Font.HELVETICA, size, style, color);
  }

  private Color white() {
    return new Color(247, 244, 239);
  }

  private String firstImage(List<Category> categories, List<Product> products) {
    for (Category category : categories) {
      if (category.image != null && !category.image.isBlank()) return category.image;
    }
    return firstProductImage(products);
  }

  private String firstProductImage(List<Product> products) {
    return products.stream()
        .map(product -> product.image)
        .filter(image -> image != null && !image.isBlank())
        .findFirst()
        .orElse("");
  }

  private String categoryText(Category category) {
    return switch (category.id) {
      case "centros-entretenimiento" -> "Piezas pensadas para integrar tecnología, almacenamiento y atmósfera en el centro social de la casa.";
      case "centros-estudio" -> "Ambientes de trabajo que equilibran concentración, orden y calidez para crear todos los días.";
      case "closets" -> "Soluciones a medida para guardar mejor, ver mejor y disfrutar rutinas más simples.";
      case "cocinas" -> "Cocinas diseñadas para transformar la rutina diaria en una experiencia de diseño.";
      case "muebles-bano" -> "Mobiliario resistente y refinado para convertir el baño en un espacio de calma.";
      case "repisas" -> "Elementos ligeros que organizan, exhiben y completan la personalidad de cada ambiente.";
      case "alcobas-infantiles" -> "Muebles seguros, flexibles y cercanos para acompañar cada etapa de crecimiento.";
      case "bibliotecas" -> "Sistemas para ordenar, exhibir y dar carácter arquitectónico a tus espacios.";
      default -> category.description == null || category.description.isBlank()
          ? "Una línea diseñada para resolver necesidades reales con calidez, orden y precisión."
          : category.description;
    };
  }

  private String subtitle(Product product) {
    List<String> parts = new ArrayList<>();
    if (product.colorFinish != null && !product.colorFinish.isBlank()) parts.add(product.colorFinish);
    if (product.material != null && !product.material.isBlank()) parts.add(product.material);
    if (!parts.isEmpty()) return String.join(" + ", parts);
    return product.description == null ? "Diseño a medida" : product.description;
  }

  private String detailLine(Product product) {
    List<String> details = new ArrayList<>();
    if (product.size != null && !product.size.isBlank()) details.add("Medidas: " + product.size);
    if (product.leadTime != null && !product.leadTime.isBlank()) details.add("Entrega: " + product.leadTime);
    return String.join(" · ", details);
  }

  private <T> List<List<T>> chunks(List<T> items, int size) {
    List<List<T>> chunks = new ArrayList<>();
    for (int index = 0; index < items.size(); index += size) {
      chunks.add(items.subList(index, Math.min(index + size, items.size())));
    }
    return chunks;
  }

  private Image image(String source) {
    if (source == null || source.isBlank()) {
      return null;
    }
    try {
      if (source.startsWith("data:image/")) {
        String base64 = source.substring(source.indexOf(',') + 1);
        if (estimatedDecodedBytes(base64) > MAX_DATA_IMAGE_BYTES) {
          return null;
        }
        return imageFromBytes(Base64.getDecoder().decode(base64));
      }
      if (source.startsWith("/uploads/")) {
        Path path = storageRoot.resolve(source.replaceFirst("^/uploads/", "")).normalize();
        if (path.startsWith(storageRoot) && Files.exists(path)) {
          return imageFromPath(path);
        }
      }
      if (source.startsWith("http://") || source.startsWith("https://")) {
        return imageFromUrl(optimizedRemoteImageUrl(source));
      }
      Path path = Path.of(source);
      if (Files.exists(path)) {
        return imageFromPath(path);
      }
    } catch (Exception ignored) {
      return null;
    }
    return null;
  }

  private Image imageFromPath(Path path) throws IOException, DocumentException {
    try (InputStream input = Files.newInputStream(path)) {
      Image optimized = imageFromBufferedImage(ImageIO.read(input));
      if (optimized != null) {
        return optimized;
      }
    }
    if (Files.size(path) <= MAX_DIRECT_IMAGE_BYTES) {
      return Image.getInstance(Files.readAllBytes(path));
    }
    return null;
  }

  private Image imageFromUrl(String source) throws IOException, DocumentException {
    URL url = URI.create(source).toURL();
    URLConnection connection = url.openConnection();
    connection.setConnectTimeout(6000);
    connection.setReadTimeout(12000);
    try (InputStream input = connection.getInputStream()) {
      Image optimized = imageFromBufferedImage(ImageIO.read(input));
      if (optimized != null) {
        return optimized;
      }
    }
    return Image.getInstance(url);
  }

  private Image imageFromBytes(byte[] bytes) throws IOException, DocumentException {
    try (java.io.ByteArrayInputStream input = new java.io.ByteArrayInputStream(bytes)) {
      Image optimized = imageFromBufferedImage(ImageIO.read(input));
      if (optimized != null) {
        return optimized;
      }
    }
    return bytes.length <= MAX_DIRECT_IMAGE_BYTES ? Image.getInstance(bytes) : null;
  }

  private Image imageFromBufferedImage(BufferedImage original) throws IOException, DocumentException {
    if (original == null) {
      return null;
    }
    BufferedImage scaled = scaleForPdf(original);
    return Image.getInstance(toJpegBytes(scaled));
  }

  private BufferedImage scaleForPdf(BufferedImage original) {
    int width = original.getWidth();
    int height = original.getHeight();
    double ratio = Math.min(1.0, (double) MAX_PDF_IMAGE_DIMENSION / Math.max(width, height));
    int targetWidth = Math.max(1, (int) Math.round(width * ratio));
    int targetHeight = Math.max(1, (int) Math.round(height * ratio));

    BufferedImage scaled = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
    Graphics2D graphics = scaled.createGraphics();
    graphics.setColor(Color.WHITE);
    graphics.fillRect(0, 0, targetWidth, targetHeight);
    graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    graphics.drawImage(original, 0, 0, targetWidth, targetHeight, null);
    graphics.dispose();
    return scaled;
  }

  private byte[] toJpegBytes(BufferedImage image) throws IOException {
    try (ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageOutputStream imageOutput = ImageIO.createImageOutputStream(output)) {
      ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
      writer.setOutput(imageOutput);
      ImageWriteParam params = writer.getDefaultWriteParam();
      params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
      params.setCompressionQuality(PDF_IMAGE_QUALITY);
      writer.write(null, new IIOImage(image, null, null), params);
      writer.dispose();
      return output.toByteArray();
    }
  }

  private long estimatedDecodedBytes(String base64) {
    return (long) Math.ceil(base64.length() * 3.0 / 4.0);
  }

  private String optimizedRemoteImageUrl(String source) {
    if (!source.contains("res.cloudinary.com") || !source.contains("/image/upload/")) {
      return source;
    }
    return source.replaceFirst("/image/upload/", "/image/upload/c_limit,w_900,h_900,q_auto:eco,f_jpg/");
  }
}
