package com.wasimasi.api.repository;

import com.wasimasi.api.dto.ConversacionNoLeidaResponseDTO;
import com.wasimasi.api.model.Mensaje;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    List<Mensaje> findByConversacionIdOrderByIdAsc(Long conversacionId);

    List<Mensaje> findByConversacionIdAndIdGreaterThanOrderByIdAsc(Long conversacionId, Long despuesDe,
                                                                 Pageable pagina);

    Optional<Mensaje> findByConversacionIdAndEmisorIdAndClienteId(Long conversacionId, Long emisorId, UUID clienteId);

    boolean existsByIdAndConversacionId(Long mensajeId, Long conversacionId);

    /** US14 - Cuenta los mensajes recibidos que superan el cursor de cada conversación. */
    @Query("""
            select new com.wasimasi.api.dto.ConversacionNoLeidaResponseDTO(c.id, count(m), max(m.id), false)
            from Mensaje m join m.conversacion c
            where m.emisor.id <> :usuarioId
              and ((c.buscador.id = :usuarioId and m.id > coalesce(c.lecturaBuscadorId, 0))
                or (c.anfitrion.id = :usuarioId and m.id > coalesce(c.lecturaAnfitrionId, 0)))
            group by c.id
            """)
    List<ConversacionNoLeidaResponseDTO> resumirNoLeidos(@Param("usuarioId") Long usuarioId);
}
