package com.wasimasi.api.dto;

import java.util.List;

/**
 * US21/US22 - Respuesta del agente: el texto, las habitaciones reales que encontró
 * (para mostrarlas como tarjetas) y, si corresponde, la acción que espera confirmación.
 */
public record RespuestaAgenteDTO(
        String respuesta,
        List<PublicacionResponseDTO> publicaciones,
        AccionAgenteDTO accionPendiente
) {
}
