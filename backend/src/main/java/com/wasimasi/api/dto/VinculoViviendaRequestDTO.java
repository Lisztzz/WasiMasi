package com.wasimasi.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** US15 - Identificador de la vivienda a la que pertenece una habitación. */
public record VinculoViviendaRequestDTO(
        @NotNull(message = "Elige una vivienda")
        @Positive(message = "El identificador de la vivienda debe ser positivo")
        Long viviendaId
) {
}
