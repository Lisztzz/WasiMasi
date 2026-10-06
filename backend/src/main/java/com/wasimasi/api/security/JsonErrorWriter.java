package com.wasimasi.api.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Escribe los errores 401/403 de Spring Security con el mismo formato
 * {timestamp, status, error, message} que usa GlobalExceptionHandler.
 * (Estos errores ocurren en los filtros, antes de llegar al controller,
 * por eso no pasan por el @RestControllerAdvice.)
 */
final class JsonErrorWriter {

    private JsonErrorWriter() {
    }

    static void escribir(HttpServletResponse response, HttpStatus status, String mensaje) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        String json = "{"
                + "\"timestamp\":\"" + LocalDateTime.now() + "\","
                + "\"status\":" + status.value() + ","
                + "\"error\":\"" + status.getReasonPhrase() + "\","
                + "\"message\":\"" + mensaje.replace("\"", "'") + "\""
                + "}";
        response.getWriter().write(json);
    }
}
