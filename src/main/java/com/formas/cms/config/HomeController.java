package com.formas.cms.config;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {
  @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
  public String home() {
    return """
        <!doctype html>
        <html lang="es">
        <head>
          <meta charset="utf-8">
          <meta name="viewport" content="width=device-width, initial-scale=1">
          <title>Formas Interiores Backend</title>
          <style>
            body {
              margin: 0;
              font-family: Arial, sans-serif;
              background: #f7f4ef;
              color: #2d2721;
            }
            main {
              max-width: 860px;
              margin: 0 auto;
              padding: 56px 24px;
            }
            .card {
              background: #fff;
              border: 1px solid #e5ddd2;
              border-radius: 12px;
              padding: 32px;
              box-shadow: 0 16px 40px rgba(45, 39, 33, 0.08);
            }
            h1 {
              margin: 0 0 12px;
              font-size: 34px;
            }
            p {
              line-height: 1.6;
            }
            .status {
              display: inline-block;
              margin: 8px 0 24px;
              padding: 8px 12px;
              border-radius: 999px;
              background: #e7f7ec;
              color: #1f7a3a;
              font-weight: 700;
            }
            .links {
              display: grid;
              gap: 12px;
              margin-top: 24px;
            }
            a {
              color: #8b5a2b;
              font-weight: 700;
              text-decoration: none;
            }
            code {
              background: #f3eee7;
              border-radius: 6px;
              padding: 2px 6px;
            }
          </style>
        </head>
        <body>
          <main>
            <section class="card">
              <h1>Formas Interiores Backend</h1>
              <div class="status">Backend encendido</div>
              <p>
                Esta es la API que alimenta el sitio y el panel de administracion.
                El sitio web se abre por separado con el frontend. Usa la URL que muestre Vite
                despues de ejecutar <code>npm run dev</code>.
              </p>
              <p>
                Si ves esta pantalla, Spring Boot esta funcionando correctamente en el puerto 8080.
              </p>
              <div class="links">
                <a href="/actuator/health">Ver estado tecnico</a>
                <a href="/api/categories">Ver categorias</a>
                <a href="/api/products">Ver productos</a>
                <a href="/h2-console">Abrir base de datos H2</a>
              </div>
            </section>
          </main>
        </body>
        </html>
        """;
  }
}
