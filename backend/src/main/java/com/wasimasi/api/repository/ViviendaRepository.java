package com.wasimasi.api.repository;

import com.wasimasi.api.model.Vivienda;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ViviendaRepository extends JpaRepository<Vivienda, Long> {
    List<Vivienda> findByAnfitrionIdOrderByNombreAsc(Long anfitrionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from Vivienda v where v.id = :id")
    Optional<Vivienda> findByIdForUpdate(Long id);
}
