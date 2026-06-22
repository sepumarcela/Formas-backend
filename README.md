# Formas Interiores Backend

API Spring Boot para administrar contenido, productos, imágenes, fichas técnicas, contactos y suscriptores de Formas Interiores.

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

Al abrir esa URL debe aparecer una pantalla de estado del backend. El sitio público se ejecuta desde el frontend con `npm run dev`.

## Variables de entorno

Para desarrollo local se puede usar H2 sin configurar nada adicional. Para producción en Render usa variables como estas:

```text
DATABASE_URL=jdbc:postgresql://HOST/DB?sslmode=require
DATABASE_USERNAME=USUARIO
DATABASE_PASSWORD=CLAVE
DATABASE_DRIVER=org.postgresql.Driver
JPA_DATABASE_PLATFORM=org.hibernate.dialect.PostgreSQLDialect
H2_CONSOLE_ENABLED=false

FRONTEND_ORIGINS=https://TU_FRONTEND,https://www.TU_FRONTEND
UPLOADS_DIR=/var/data/uploads

ADMIN_EMAIL=admin@formas.com
ADMIN_PASSWORD=CAMBIAR_CLAVE
JWT_SECRET=CAMBIAR_POR_UN_SECRETO_LARGO
JWT_EXPIRATION_MINUTES=43200

CLOUDINARY_CLOUD_NAME=TU_CLOUD_NAME
CLOUDINARY_API_KEY=TU_API_KEY
CLOUDINARY_API_SECRET=TU_API_SECRET
CLOUDINARY_FOLDER=formas
```

El frontend debe apuntar al backend con:

```text
VITE_API_BASE_URL=https://TU_BACKEND
```

## Despliegue en Render

1. Crear un Web Service conectado al repositorio del backend.
2. Usar Docker. Render detecta el `Dockerfile`.
3. Configurar:

```text
Health Check Path: /actuator/health
```

4. Agregar las variables de entorno de producción.
5. Para fichas técnicas PDF, usa disco persistente en Render:

```text
Mount Path: /var/data
UPLOADS_DIR=/var/data/uploads
```

## Almacenamiento

- Imágenes: Cloudinary cuando `CLOUDINARY_*` está configurado.
- Fichas técnicas PDF: filesystem del backend, bajo `UPLOADS_DIR/fichas-tecnicas`.
- Datos: PostgreSQL/Neon en producción.

Las fichas técnicas se sirven inline desde:

```text
GET /api/technical-sheets/{archivo.pdf}
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

```text
POST /api/contact-submissions
GET  /api/contact-submissions

POST /api/newsletter-subscriptions
GET  /api/newsletter-subscriptions
```

Los correos suscritos se guardan en la tabla `newsletter_subscription`.

## Importación masiva de productos

```text
POST /api/import/products/excel
```

Campo multipart:

```text
file
```

Columnas principales:

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
ficha_tecnica
technical_sheet
ficha_pdf
destacado
activo
```

## Importación masiva de imágenes

```text
POST /api/import/images/zip
```

Convención:

```text
Producto id: forma-tv-180
Imagen: forma-tv-180.jpg
```

## Importación masiva de fichas técnicas

```text
POST /api/import/products/technical-sheets/zip
```

Convención:

```text
Producto id: repisa-002
Ficha PDF: repisa-002.pdf
```

Después de subir el ZIP, el producto guarda una URL como:

```text
/api/technical-sheets/repisa-002.pdf
```