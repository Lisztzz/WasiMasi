package com.wasimasi.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/** US07 y US18 - Estado solicitado por el anfitrión. */
public record EstadoPublicacionRequestDTO(
        @NotNull(message = "El estado es obligatorio")
        @Pattern(regexp = "PAUSADA|ACTIVA|OCUPADA", message = "El estado debe ser PAUSADA, ACTIVA u OCUPADA")
        String estado
) {
}
