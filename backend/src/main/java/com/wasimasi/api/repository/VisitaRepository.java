package com.wasimasi.api.repository;

import com.wasimasi.api.model.EstadoVisita;
import com.wasimasi.api.model.Visita;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface VisitaRepository extends JpaRepository<Visita, Long> {

    // US16 - evitar duplicados pendientes
    boolean existsByBuscadorIdAndPublicacionIdAndEstado(Long buscadorId, Long publicacionId, EstadoVisita estado);

    // US17 - bandeja del anfitrión
    List<Visita> findByPublicacionAnfitrionIdOrderByCreadaEnDesc(Long anfitrionId);

    // US19 - interesados de una publicación
    List<Visita> findByPublicacionIdOrderByCreadaEnDesc(Long publicacionId);

    List<Visita> findByPublicacionIdAndEstado(Long publicacionId, EstadoVisita estado);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Visita> findVisitaById(Long id);
}
