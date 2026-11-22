package com.wasimasi.api.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/** US20 - Indicadores de demanda y oferta para un rango de presupuesto. */
@Getter
@Builder
public class ReportePrecioDTO {
    private BigDecimal precioMin;
    private BigDecimal precioMax;
    private long busquedas;
    private long publicacionesActivas;
}
