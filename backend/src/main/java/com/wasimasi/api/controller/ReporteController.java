package com.wasimasi.api.controller;

import com.wasimasi.api.dto.ReportePrecioDTO;
import com.wasimasi.api.dto.ReporteZonaDTO;
import com.wasimasi.api.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** US20 - Indicadores agregados de búsqueda y oferta de habitaciones. */
@RestController
@RequestMapping("/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;

    /** US20 - Zonas con actividad de búsqueda, publicaciones y solicitudes. */
    @GetMapping("/zonas")
    public List<ReporteZonaDTO> zonas() {
        return reporteService.porZona();
    }

    /** US20 - Rangos de presupuesto buscados y habitaciones disponibles. */
    @GetMapping("/precios")
    public List<ReportePrecioDTO> precios() {
        return reporteService.porRangoPrecio();
    }
}
