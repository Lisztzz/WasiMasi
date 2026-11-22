package com.wasimasi.api.dto;

import java.util.List;

/** US15 - Grupo y lote de mensajes que puede consultar un integrante. */
public record DetalleGrupoResponseDTO(GrupoResponseDTO conversacion, List<MensajeGrupoResponseDTO> mensajes) {
}
