package com.wasimasi.api.dto;

/** US14 - Cantidad de mensajes sin leer y último pendiente de una conversación. */
public record ConversacionNoLeidaResponseDTO(Long conversacionId, long cantidad, Long ultimoMensajeId, boolean grupal) {
}
