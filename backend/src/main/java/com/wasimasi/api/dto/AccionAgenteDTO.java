package com.wasimasi.api.dto;

import com.wasimasi.api.model.EstadoAccionAgente;
import com.wasimasi.api.model.TipoAccionAgente;

import java.time.LocalDateTime;

/** US22 - Acción propuesta por el agente, con el estado de la decisión del usuario. */
public record AccionAgenteDTO(
        Long id,
        TipoAccionAgente tipo,
        EstadoAccionAgente estado,
        Long publicacionId,
        String zona,
        LocalDateTime fechaHora,
        String comentario,
        Long visitaId
) {
}
