package com.wasimasi.api.dto;

import com.wasimasi.api.model.EstadoVisita;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** US17 - Decisión del anfitrión sobre una solicitud pendiente. */
@Getter
@Setter
public class VisitaRespuestaRequestDTO {

    @NotNull(message = "Selecciona si confirmas o rechazas la visita")
    private EstadoVisita estado;

    @Size(max = 500, message = "El motivo no puede superar los 500 caracteres")
    private String motivoRespuesta;
}
