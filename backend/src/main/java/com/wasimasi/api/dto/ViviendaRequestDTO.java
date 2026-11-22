package com.wasimasi.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** US15 - Nombre con que el anfitrión reconoce una vivienda. */
public record ViviendaRequestDTO(
        @NotBlank(message = "Escribe un nombre para la vivienda")
        @Size(max = 100, message = "El nombre debe tener como máximo 100 caracteres")
        String nombre
) {
}
