package com.wasimasi.api.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** US16 - Datos para solicitar una visita. */
@Getter
@Setter
public class VisitaRequestDTO {

    @NotNull(message = "Selecciona la publicación que quieres visitar")
    private Long publicacionId;

    @NotNull(message = "Indica la fecha y hora que prefieres")
    @Future(message = "La fecha de visita debe ser futura")
    private LocalDateTime fechaHora;

    @Size(max = 500, message = "El comentario no puede superar los 500 caracteres")
    private String comentario;
}
