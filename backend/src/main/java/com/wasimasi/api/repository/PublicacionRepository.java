package com.wasimasi.api.repository;

import com.wasimasi.api.dto.ConteoZonaDTO;
import com.wasimasi.api.model.EstadoPublicacion;
import com.wasimasi.api.model.Publicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface PublicacionRepository extends JpaRepository<Publicacion, Long> {

    /** US08 - Buscar habitaciones por zona y presupuesto, solo entre las publicaciones activas. */
    List<Publicacion> findByEstadoAndZonaIgnoreCaseAndPrecioBetween(
            EstadoPublicacion estado, String zona, BigDecimal precioMin, BigDecimal precioMax);

    /** US20 - Contar publicaciones activas por zona para los reportes de oferta. */
    @Query("""
            select new com.wasimasi.api.dto.ConteoZonaDTO(p.zona, count(p))
            from Publicacion p
            where p.estado = com.wasimasi.api.model.EstadoPublicacion.ACTIVA
            group by p.zona
            """)
    List<ConteoZonaDTO> contarActivasPorZona();

    /** US20 - Contar habitaciones activas dentro de un presupuesto buscado. */
    long countByEstadoAndPrecioBetween(EstadoPublicacion estado, BigDecimal precioMin, BigDecimal precioMax);
}
