package com.wasimasi.api.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/** US07 - Datos editables de una publicación. */
public record EditarPublicacionRequestDTO(
        @Size(max = 255, message = "El título no puede superar los 255 caracteres")
        String titulo,

        @NotBlank(message = "La zona es obligatoria")
        @Size(max = 255, message = "La zona no puede superar los 255 caracteres")
        String zona,

        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
        @Digits(integer = 8, fraction = 2, message = "El precio admite hasta 8 enteros y 2 decimales")
        BigDecimal precio,

        @Size(max = 1000, message = "La descripción no puede superar los 1000 caracteres")
        String descripcion,

        @NotEmpty(message = "Agrega al menos una foto")
        List<@NotBlank(message = "La URL de la foto es obligatoria")
             @Size(max = 255, message = "La URL de la foto no puede superar los 255 caracteres")
             String> fotos,

        /** US09 - Condiciones de la habitacion (opcionales). */
        Boolean mascotasPermitidas,
        String generoPreferido,
        String servicios,

        /** US10 - Coordenadas para el mapa (opcionales). */
        @DecimalMin(value = "-90.0", message = "La latitud debe estar entre -90 y 90")
        @DecimalMax(value = "90.0", message = "La latitud debe estar entre -90 y 90")
        BigDecimal latitud,

        @DecimalMin(value = "-180.0", message = "La longitud debe estar entre -180 y 180")
        @DecimalMax(value = "180.0", message = "La longitud debe estar entre -180 y 180")
        BigDecimal longitud
) {
}
