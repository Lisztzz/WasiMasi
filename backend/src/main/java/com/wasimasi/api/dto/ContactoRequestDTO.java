package com.wasimasi.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** US12 - Primer mensaje desde el detalle de una publicación. */
public record ContactoRequestDTO(
        @NotBlank(message = "Escribe un mensaje antes de enviarlo")
        @Size(max = 1000, message = "El mensaje debe tener como máximo 1000 caracteres")
        String mensaje
) {
}
