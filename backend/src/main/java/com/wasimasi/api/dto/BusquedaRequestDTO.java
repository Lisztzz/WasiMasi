package com.wasimasi.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/** US08/US20 - Filtros de búsqueda que se registran para reportes agregados. */
@Getter
@Setter
public class BusquedaRequestDTO {

    @NotBlank(message = "Indica una zona para buscar")
    @Size(max = 100, message = "La zona no puede superar los 100 caracteres")
    private String zona;

    @NotNull(message = "Indica el precio mínimo")
    @DecimalMin(value = "0.0", message = "El precio mínimo no puede ser negativo")
    @Digits(integer = 8, fraction = 2, message = "El precio mínimo admite hasta dos decimales")
    private BigDecimal precioMin;

    @NotNull(message = "Indica el precio máximo")
    @DecimalMin(value = "0.0", message = "El precio máximo no puede ser negativo")
    @Digits(integer = 8, fraction = 2, message = "El precio máximo admite hasta dos decimales")
    private BigDecimal precioMax;

    /** US09 - Condiciones opcionales (mismos valores que GET /publicaciones). */
    private Boolean mascotas;

    private String genero;

    private String servicios;
}
