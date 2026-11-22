package com.wasimasi.api.repository;

import com.wasimasi.api.model.AccionAgente;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface AccionAgenteRepository extends JpaRepository<AccionAgente, Long> {

    /** US22 - Una acción solo la puede confirmar o cancelar quien la recibió; se bloquea para no ejecutarla dos veces. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<AccionAgente> findByIdAndUsuarioId(Long id, Long usuarioId);
}
