package com.wasimasi.api.dto;

import java.math.BigDecimal;

/** US20 - Proyección interna de búsquedas agrupadas por presupuesto exacto. */
public record ConteoRangoPrecioDTO(BigDecimal precioMin, BigDecimal precioMax, Long total) {
}
