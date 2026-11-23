package com.wasimasi.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/** US13 - Mensaje con un identificador que se reutiliza al reintentar. */
public record MensajeRequestDTO(
        @NotNull(message = "El identificador del envío es obligatorio") UUID clienteId,
        @NotBlank(message = "Escribe un mensaje antes de enviarlo")
        @Size(max = 1000, message = "El mensaje debe tener como máximo 1000 caracteres")
        String contenido
) {
}
