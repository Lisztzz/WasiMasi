package com.wasimasi.api.repository;

import com.wasimasi.api.dto.ConteoZonaDTO;
import com.wasimasi.api.model.EstadoPublicacion;
import com.wasimasi.api.model.Publicacion;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PublicacionRepository extends JpaRepository<Publicacion, Long>,
        JpaSpecificationExecutor<Publicacion> {

    /** US08 - Buscar habitaciones por zona y presupuesto, solo entre las publicaciones activas. */
    List<Publicacion> findByEstadoAndZonaIgnoreCaseAndPrecioBetween(
            EstadoPublicacion estado, String zona, BigDecimal precioMin, BigDecimal precioMax);

    /** US07 - Listar las publicaciones propias, incluidas las no disponibles. */
    List<Publicacion> findByAnfitrionIdOrderByFechaPublicacionDescIdDesc(Long anfitrionId);

    /** US07 y US18 - Serializar los cambios de datos y estado de una misma publicación. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Publicacion> findPublicacionById(Long id);

    /** US15 - Saber si una vivienda todavia tiene habitaciones activas. */
    boolean existsByViviendaIdAndEstado(Long viviendaId, EstadoPublicacion estado);
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
