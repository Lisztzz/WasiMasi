package com.wasimasi.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * Datos de entrada para US06 - Publicar una habitacion.
 * El anfitrion NO se envia en el body: se toma del usuario autenticado (JWT),
 * asi nadie puede publicar a nombre de otro.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PublicacionRequestDTO {

    @NotBlank(message = "La zona es obligatoria")
    private String zona;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
    private BigDecimal precio;

    private String descripcion;

    private List<String> fotos;
}
