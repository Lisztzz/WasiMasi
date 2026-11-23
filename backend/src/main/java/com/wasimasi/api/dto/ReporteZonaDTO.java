package com.wasimasi.api.dto;

import lombok.Builder;
import lombok.Getter;

/** US20 - Indicadores de demanda y oferta para una zona. */
@Getter
@Builder
public class ReporteZonaDTO {
    private String zona;
    private long busquedas;
    private long publicacionesActivas;
    private long solicitudesVisita;
}
