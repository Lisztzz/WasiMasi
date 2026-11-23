package com.wasimasi.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** US21 - Un turno anterior de la conversación, para que el agente tenga contexto. */
public record MensajeAgenteDTO(
        @NotBlank
        @Pattern(regexp = "USUARIO|ASISTENTE", message = "El rol debe ser USUARIO o ASISTENTE")
        String rol,

        @NotBlank
        @Size(max = 2000, message = "Cada mensaje del historial admite hasta 2000 caracteres")
        String contenido
) {
}
