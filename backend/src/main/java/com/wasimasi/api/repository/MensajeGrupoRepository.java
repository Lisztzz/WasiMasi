package com.wasimasi.api.repository;

import com.wasimasi.api.dto.ConversacionNoLeidaResponseDTO;
import com.wasimasi.api.model.MensajeGrupo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MensajeGrupoRepository extends JpaRepository<MensajeGrupo, Long> {
    @EntityGraph(attributePaths = "emisor")
    List<MensajeGrupo> findByViviendaIdOrderByIdAsc(Long viviendaId);

    @EntityGraph(attributePaths = "emisor")
    List<MensajeGrupo> findByViviendaIdAndIdGreaterThanOrderByIdAsc(Long viviendaId, Long despuesDe, Pageable pagina);

    Optional<MensajeGrupo> findByViviendaIdAndEmisorIdAndClienteId(Long viviendaId, Long emisorId, UUID clienteId);

    boolean existsByIdAndViviendaId(Long mensajeId, Long viviendaId);

    @Query("""
            select new com.wasimasi.api.dto.ConversacionNoLeidaResponseDTO(m.vivienda.id, count(m), max(m.id), true)
            from MensajeGrupo m, MiembroGrupo p
            where p.usuario.id = :usuarioId and p.vivienda = m.vivienda
              and m.emisor.id <> :usuarioId and m.id > coalesce(p.ultimaLecturaId, 0)
            group by m.vivienda.id
            """)
    List<ConversacionNoLeidaResponseDTO> resumirNoLeidos(Long usuarioId);
}
