package com.wasimasi.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/** US15 - Mensaje con el nombre del participante que lo envió. */
public record MensajeGrupoResponseDTO(
        Long id, Long emisorId, String emisorNombre, String contenido, LocalDateTime fechaEnvio, UUID clienteId
) {
}
