# WasiMasi — Landing Page v1

Landing Page de WasiMasi (TB3, 1ACC0236 Ingeniería de Software, UPC).
HTML + CSS + JavaScript, sin frameworks. Diseño: guía de estilo y mock-up en Figma (Material Design 3).

## Ejecutar

1. Abrir la carpeta `landing/` en VS Code.
2. Clic derecho en `index.html` → **Open with Live Server** → http://127.0.0.1:5500

El backend ya permite este origen en CORS (`http://127.0.0.1:5500`).

## Estructura

- `index.html` — página principal: navegación, hero, cifras, cómo funciona, seguridad, testimonios, llamada a la acción y footer
- `terminos.html` — Términos de Servicio y Política de privacidad
- `css/styles.css` — variables de color, tipografía Roboto y diseño responsive (corte en 768 px)
- `js/main.js` — selector de idioma ES/EN (se recuerda en el navegador) y menú mobile
- `img/logo.svg` — logo de WasiMasi

## Enlaces a la Web App

Los botones llevan a la Web App de Angular en http://localhost:4200: `/login`, `/registro`, `/buscar` y `/publicar`.

## Accesibilidad

HTML semántico con un solo `h1`, enlace "Saltar al contenido", foco visible, `aria-label` y `aria-expanded` en el menú,
`aria-pressed` en el selector de idioma, `lang` actualizado al cambiar de idioma y contraste WCAG AA en todos los textos.
