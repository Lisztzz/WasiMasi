package com.wasimasi.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/** US21 - Pregunta en lenguaje natural al agente de IA. */
public record ConsultaAgenteRequestDTO(
        @NotBlank(message = "Escribe tu consulta")
        @Size(max = 1000, message = "La consulta admite hasta 1000 caracteres")
        String mensaje,

        /** Últimos turnos de la conversación (opcional, máximo 10). */
        @Size(max = 10, message = "El historial admite como máximo 10 mensajes")
        List<@Valid MensajeAgenteDTO> historial
) {
}
