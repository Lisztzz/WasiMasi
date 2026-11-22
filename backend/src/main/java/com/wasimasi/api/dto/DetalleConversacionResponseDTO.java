package com.wasimasi.api.dto;

import java.util.List;

/** US12 - Conversación e historial para la vista de mensajes. */
public record DetalleConversacionResponseDTO(
        ConversacionResponseDTO conversacion,
        List<MensajeResponseDTO> mensajes
) {
}
