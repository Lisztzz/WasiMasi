package com.wasimasi.api.dto;

import java.time.LocalDateTime;

/** US15 - Resumen del chat de una vivienda y estado de su historial. */
public record GrupoResponseDTO(Long id, String interlocutorNombre, LocalDateTime fechaCreacion, boolean cerrada) {
}
