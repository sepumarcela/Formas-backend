# FORMAS Backend

API Spring Boot para administrar el contenido de FORMAS.

## Requisitos

- Java JDK 21
- Maven 3.9+

## Ejecutar localmente

```powershell
mvn spring-boot:run
```

La API queda disponible en:

```text
http://localhost:8080
```

Al abrir esa URL en el navegador debe aparecer una pantalla de estado del backend.
El sitio web visible para el usuario final se abre desde el frontend. Para verlo, ejecuta `npm run dev` en la carpeta del frontend y abre la URL que muestre Vite en la terminal.

En resumen:

```text
Backend / API: http://localhost:8080
Frontend / sitio web: la URL que muestre Vite al ejecutar npm run dev
```

La base de datos local usa H2 y se guarda en:

```text
formas-backend/data/formas-cms.mv.db
```

La consola de H2 queda en:

```text
http://localhost:8080/h2-console
```

Datos de conexión:

```text
JDBC URL: jdbc:h2:file:./data/formas-cms
User: sa
Password:
```

## Endpoints principales

```text
GET    /api/products
POST   /api/products
PUT    /api/products/{id}
DELETE /api/products/{id}

GET    /api/categories
POST   /api/categories
PUT    /api/categories/{id}
DELETE /api/categories/{id}

GET    /api/pages
GET    /api/hero-slides
GET    /api/projects
GET    /api/testimonials
GET    /api/blog-posts
```

## Contacto y newsletter

Los mensajes enviados desde la pagina de contacto se guardan en:

```text
POST /api/contact-submissions
GET  /api/contact-submissions
```

Los correos de suscripcion del blog se guardan en:

```text
POST /api/newsletter-subscriptions
GET  /api/newsletter-subscriptions
```

Estos endpoints guardan datos en la base configurada, por ejemplo Neon/PostgreSQL.

## Importación masiva de productos

Endpoint:

```text
POST /api/import/products/excel
```

Campo multipart:

```text
file
```

Columnas esperadas en el Excel:

```text
id
categoria_id
nombre
precio_texto
precio_neto
medidas
descripcion
material
color_acabado
tiempo_entrega
descuento_porcentaje
descuento_texto
descuento_inicio
descuento_fin
destacado
activo
```

Ejemplo:

```text
id: forma-tv-180
categoria_id: centros-entretenimiento
nombre: FORMA TV-180
precio_texto: $4.500.000
precio_neto: 4500000
descuento_porcentaje: 15
descuento_texto: -15%
descuento_inicio: 2026-06-01
descuento_fin: 2026-06-30
destacado: true
activo: true
```

## Importación masiva de imágenes

Endpoint:

```text
POST /api/import/images/zip
```

También se puede subir una sola imagen desde el panel de administración:

```text
POST /api/import/images/file
```

Campos multipart:

```text
folder=productos
file=imagen.jpg
```

Las imágenes quedan guardadas físicamente en:

```text
formas-backend/uploads
```

Y se sirven públicamente desde:

```text
/uploads/{folder}/{archivo}
```

Campos multipart:

```text
folder=productos
file=imagenes-productos.zip
```

Convención:

```text
Producto id: forma-tv-180
Imagen: forma-tv-180.jpg
URL final: /uploads/productos/forma-tv-180.jpg
```

También se pueden usar carpetas como:

```text
categorias
proyectos
testimonios
blog
inicio
```
