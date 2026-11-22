package com.wasimasi.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** US13 - Último mensaje recuperado en el chat abierto. */
public record LecturaRequestDTO(
        @NotNull(message = "El identificador del mensaje es obligatorio")
        @Positive(message = "El identificador del mensaje debe ser positivo") Long mensajeId
) {
}
