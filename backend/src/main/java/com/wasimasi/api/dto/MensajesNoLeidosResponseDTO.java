package com.wasimasi.api.dto;

import java.util.List;

/** US13/US14 - Indicador de entrada y resumen de mensajes sin leer. */
public record MensajesNoLeidosResponseDTO(
        boolean disponibles,
        long total,
        List<ConversacionNoLeidaResponseDTO> conversaciones
) {
}
