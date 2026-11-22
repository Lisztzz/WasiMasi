package com.wasimasi.api.dto;

import com.wasimasi.api.model.EstadoVisita;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/** US16/US17 - Vista segura de una solicitud de visita. */
@Getter
@Builder
public class VisitaResponseDTO {
    private Long id;
    private Long publicacionId;
    private String zona;
    private Long buscadorId;
    private String buscadorNombre;
    private String anfitrionNombre;
    private LocalDateTime fechaHora;
    private EstadoVisita estado;
    private String comentario;
    private String motivoRespuesta;
    private LocalDateTime creadaEn;
    private LocalDateTime actualizadaEn;
}
