package com.wasimasi.api.exception;

import com.wasimasi.api.dto.RegistroRequestDTO;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests del formato de error estandar {timestamp, status, error, message}
 * que consume el frontend. No se levanta ningun controlador ni servidor.
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("BadRequestException => 400")
    void handleBadRequest() {
        LocalDateTime antes = LocalDateTime.now();
        ResponseEntity<ErrorResponseDTO> respuesta =
                handler.handleBadRequest(new BadRequestException("El precio debe ser mayor a 0"));

        verificarCuerpo(respuesta, HttpStatus.BAD_REQUEST, "El precio debe ser mayor a 0", antes);
    }

    @Test
    @DisplayName("UnauthorizedException => 401")
    void handleUnauthorized() {
        LocalDateTime antes = LocalDateTime.now();
        ResponseEntity<ErrorResponseDTO> respuesta =
                handler.handleUnauthorized(new UnauthorizedException("Correo o contrasena incorrectos"));

        verificarCuerpo(respuesta, HttpStatus.UNAUTHORIZED, "Correo o contrasena incorrectos", antes);
    }

    @Test
    @DisplayName("ForbiddenException => 403")
    void handleForbidden() {
        LocalDateTime antes = LocalDateTime.now();
        ResponseEntity<ErrorResponseDTO> respuesta =
                handler.handleForbidden(new ForbiddenException("Solo puedes verificar tu propia cuenta"));

        verificarCuerpo(respuesta, HttpStatus.FORBIDDEN, "Solo puedes verificar tu propia cuenta", antes);
    }

    @Test
    @DisplayName("ResourceNotFoundException => 404")
    void handleNotFound() {
        LocalDateTime antes = LocalDateTime.now();
        ResponseEntity<ErrorResponseDTO> respuesta =
                handler.handleNotFound(new ResourceNotFoundException("No se encontro la publicacion con id 9"));

        verificarCuerpo(respuesta, HttpStatus.NOT_FOUND, "No se encontro la publicacion con id 9", antes);
    }

    @Test
    @DisplayName("ConflictException => 409")
    void handleConflict() {
        LocalDateTime antes = LocalDateTime.now();
        ResponseEntity<ErrorResponseDTO> respuesta =
                handler.handleConflict(new ConflictException("Ya existe una cuenta con este correo"));

        verificarCuerpo(respuesta, HttpStatus.CONFLICT, "Ya existe una cuenta con este correo", antes);
    }

    @Test
    @DisplayName("ConstraintViolationException => 400 con el mensaje original")
    void handleConstraintViolation() {
        LocalDateTime antes = LocalDateTime.now();
        ResponseEntity<ErrorResponseDTO> respuesta = handler.handleConstraintViolation(
                new ConstraintViolationException("El precio debe ser mayor a 0", Set.of()));

        verificarCuerpo(respuesta, HttpStatus.BAD_REQUEST, "El precio debe ser mayor a 0", antes);
    }

    @Test
    @DisplayName("MethodArgumentNotValidException => 400 con los errores de campo")
    void handleValidacion() throws Exception {
        BeanPropertyBindingResult errores = new BeanPropertyBindingResult(new RegistroRequestDTO(), "registro");
        errores.addError(new FieldError("registro", "correo", "formato invalido"));
        errores.addError(new FieldError("registro", "nombre", "obligatorio"));
        MethodParameter parametro = new MethodParameter(
                GlobalExceptionHandlerTest.class.getDeclaredMethod("destino", RegistroRequestDTO.class), 0);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parametro, errores);

        LocalDateTime antes = LocalDateTime.now();
        ResponseEntity<ErrorResponseDTO> respuesta = handler.handleValidacion(ex);

        verificarCuerpo(respuesta, HttpStatus.BAD_REQUEST,
                "correo: formato invalido; nombre: obligatorio", antes);
    }

    /** Metodo auxiliar para construir el MethodParameter de la validacion. */
    private static void destino(RegistroRequestDTO request) {
        // No se ejecuta: solo sirve para simular el parametro de un controlador.
    }

    private static void verificarCuerpo(ResponseEntity<ErrorResponseDTO> respuesta, HttpStatus esperado,
                                        String mensaje, LocalDateTime antes) {
        LocalDateTime despues = LocalDateTime.now();
        assertThat(respuesta.getStatusCode()).isEqualTo(esperado);

        ErrorResponseDTO cuerpo = respuesta.getBody();
        assertThat(cuerpo).isNotNull();
        assertThat(cuerpo.getStatus()).isEqualTo(esperado.value());
        assertThat(cuerpo.getError()).isEqualTo(esperado.getReasonPhrase());
        assertThat(cuerpo.getMessage()).isEqualTo(mensaje);
        assertThat(cuerpo.getTimestamp()).isBetween(antes, despues);
    }
}
