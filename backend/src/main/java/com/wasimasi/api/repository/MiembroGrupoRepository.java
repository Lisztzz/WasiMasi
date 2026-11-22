package com.wasimasi.api.repository;

import com.wasimasi.api.model.MiembroGrupo;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MiembroGrupoRepository extends JpaRepository<MiembroGrupo, Long> {
    Optional<MiembroGrupo> findByViviendaIdAndUsuarioId(Long viviendaId, Long usuarioId);

    @EntityGraph(attributePaths = "vivienda")
    List<MiembroGrupo> findByUsuarioIdOrderByViviendaGrupoCreadoEnDesc(Long usuarioId);

    boolean existsByViviendaIdAndUsuarioId(Long viviendaId, Long usuarioId);
}
