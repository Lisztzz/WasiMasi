package com.wasimasi.api.repository;

import com.wasimasi.api.model.Conversacion;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversacionRepository extends JpaRepository<Conversacion, Long> {

    Optional<Conversacion> findByBuscadorIdAndAnfitrionId(Long buscadorId, Long anfitrionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Conversacion c where c.id = :id")
    Optional<Conversacion> findByIdForUpdate(@Param("id") Long id);

    @EntityGraph(attributePaths = {"buscador", "anfitrion"})
    List<Conversacion> findByBuscadorIdOrAnfitrionIdOrderByIdDesc(Long buscadorId, Long anfitrionId);
}
