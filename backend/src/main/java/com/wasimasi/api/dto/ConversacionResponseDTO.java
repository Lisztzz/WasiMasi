package com.wasimasi.api.dto;

import java.time.LocalDateTime;

/** US12 - Entrada de la bandeja, desde la perspectiva del participante. */
public record ConversacionResponseDTO(Long id, String interlocutorNombre, LocalDateTime fechaCreacion) {
}
