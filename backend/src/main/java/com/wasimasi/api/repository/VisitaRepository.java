package com.wasimasi.api.repository;

import com.wasimasi.api.dto.ConteoZonaDTO;
import com.wasimasi.api.model.EstadoVisita;
import com.wasimasi.api.model.Visita;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface VisitaRepository extends JpaRepository<Visita, Long> {

    // US16 - evitar duplicados pendientes
    boolean existsByBuscadorIdAndPublicacionIdAndEstado(Long buscadorId, Long publicacionId, EstadoVisita estado);

    // US17 - bandeja del anfitrión
    List<Visita> findByPublicacionAnfitrionIdOrderByCreadaEnDesc(Long anfitrionId);

    // US19 - interesados de una publicación
    List<Visita> findByPublicacionIdOrderByCreadaEnDesc(Long publicacionId);

    /** US20 - Solicitudes de visita por zona, como indicador agregado de interés. */
    @Query("""
            select new com.wasimasi.api.dto.ConteoZonaDTO(v.publicacion.zona, count(v))
            from Visita v
            group by v.publicacion.zona
            """)
    List<ConteoZonaDTO> contarPorZona();

    /** US15 - Visitas confirmadas de una habitacion (miembros del chat grupal). */
    List<Visita> findByPublicacionIdAndEstado(Long publicacionId, EstadoVisita estado);

    /** US17 - Bloquear la solicitud mientras se responde. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Visita> findVisitaById(Long id);
}
