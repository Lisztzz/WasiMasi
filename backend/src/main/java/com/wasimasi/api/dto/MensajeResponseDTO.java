package com.wasimasi.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/** US12/US13 - Mensaje visible para los participantes de la conversación. */
public record MensajeResponseDTO(Long id, Long emisorId, String contenido, LocalDateTime fechaEnvio, UUID clienteId) {
}
