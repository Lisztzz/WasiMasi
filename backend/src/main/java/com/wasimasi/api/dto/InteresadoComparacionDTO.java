package com.wasimasi.api.dto;

import com.wasimasi.api.model.EstadoVerificacion;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/** US19 - Datos necesarios para comparar estudiantes que solicitaron visita. */
@Getter
@Builder
public class InteresadoComparacionDTO {
    private Long visitaId;
    private Long buscadorId;
    private String nombre;
    private String universidad;
    private EstadoVerificacion estadoVerificacion;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaHoraVisita;
    private String estadoVisita;
    private String comentario;
}
