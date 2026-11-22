package com.wasimasi.api.service;

import com.wasimasi.api.dto.ConteoRangoPrecioDTO;
import com.wasimasi.api.dto.ConteoZonaDTO;
import com.wasimasi.api.dto.ReportePrecioDTO;
import com.wasimasi.api.dto.ReporteZonaDTO;
import com.wasimasi.api.model.EstadoPublicacion;
import com.wasimasi.api.repository.BusquedaRepository;
import com.wasimasi.api.repository.PublicacionRepository;
import com.wasimasi.api.repository.VisitaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Indicadores agregados de búsqueda y oferta de US20. */
@Service
@RequiredArgsConstructor
public class ReporteService {

    private static final int MAX_RANGOS = 10;

    private final BusquedaRepository busquedaRepository;
    private final PublicacionRepository publicacionRepository;
    private final VisitaRepository visitaRepository;

    /** US20 - Ordena zonas por búsquedas e incluye oferta y solicitudes recibidas. */
    @Transactional(readOnly = true)
    public List<ReporteZonaDTO> porZona() {
        Map<String, ResumenZona> resumen = new LinkedHashMap<>();
        acumular(resumen, publicacionRepository.contarActivasPorZona(), TipoConteo.PUBLICACIONES);
        acumular(resumen, busquedaRepository.contarPorZona(), TipoConteo.BUSQUEDAS);
        acumular(resumen, visitaRepository.contarPorZona(), TipoConteo.VISITAS);

        return resumen.values().stream()
                .sorted(Comparator.comparingLong((ResumenZona zona) -> zona.busquedas).reversed()
                        .thenComparing(Comparator.comparingLong((ResumenZona zona) -> zona.visitas).reversed())
                        .thenComparing(Comparator.comparingLong((ResumenZona zona) -> zona.publicaciones).reversed())
                        .thenComparing(zona -> zona.nombre.toLowerCase(Locale.ROOT)))
                .map(zona -> ReporteZonaDTO.builder()
                        .zona(zona.nombre)
                        .busquedas(zona.busquedas)
                        .publicacionesActivas(zona.publicaciones)
                        .solicitudesVisita(zona.visitas)
                        .build())
                .toList();
    }

    /** US20 - Presenta los presupuestos más consultados y la oferta que cabe en cada uno. */
    @Transactional(readOnly = true)
    public List<ReportePrecioDTO> porRangoPrecio() {
        List<ReportePrecioDTO> reportes = new ArrayList<>();
        List<ConteoRangoPrecioDTO> rangos = busquedaRepository.contarPorRangoPrecio();
        for (ConteoRangoPrecioDTO rango : rangos.stream().limit(MAX_RANGOS).toList()) {
            long publicaciones = publicacionRepository.countByEstadoAndPrecioBetween(
                    EstadoPublicacion.ACTIVA, rango.precioMin(), rango.precioMax());
            reportes.add(ReportePrecioDTO.builder()
                    .precioMin(rango.precioMin())
                    .precioMax(rango.precioMax())
                    .busquedas(rango.total())
                    .publicacionesActivas(publicaciones)
                    .build());
        }
        return List.copyOf(reportes);
    }

    private void acumular(Map<String, ResumenZona> resumen, List<ConteoZonaDTO> conteos, TipoConteo tipo) {
        for (ConteoZonaDTO conteo : conteos) {
            String nombre = conteo.zona().trim();
            String clave = normalizarClaveZona(nombre);
            ResumenZona actual = resumen.computeIfAbsent(clave, ignorada -> new ResumenZona(nombre));
            switch (tipo) {
                case BUSQUEDAS -> actual.busquedas += conteo.total();
                case PUBLICACIONES -> actual.publicaciones += conteo.total();
                case VISITAS -> actual.visitas += conteo.total();
            }
        }
    }

    private String normalizarClaveZona(String zona) {
        String compacta = zona.trim().replaceAll("\\s+", " ");
        return Normalizer.normalize(compacta, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT);
    }

    private enum TipoConteo { BUSQUEDAS, PUBLICACIONES, VISITAS }

    private static final class ResumenZona {
        private final String nombre;
        private long busquedas;
        private long publicaciones;
        private long visitas;

        private ResumenZona(String nombre) {
            this.nombre = nombre;
        }
    }
}
