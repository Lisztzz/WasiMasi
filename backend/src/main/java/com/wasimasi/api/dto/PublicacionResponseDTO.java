package com.wasimasi.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wasimasi.api.model.EstadoPublicacion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Datos de salida de una publicacion (US08 - listado, US11 - detalle). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicacionResponseDTO {
    private Long id;
    private Long anfitrionId;
    private String anfitrionNombre;
    private boolean anfitrionVerificado;
    private String zona;
    private BigDecimal precio;
    private String descripcion;
    private List<String> fotos;
    private EstadoPublicacion estado;
    private LocalDateTime fechaPublicacion;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String titulo;

    /** US09 - Condiciones negociables/no negociables. */
    private Boolean mascotasPermitidas;

    private String generoPreferido; // 'M', 'F', 'Any'

    private String servicios; // Coma-separado: "agua,electricidad,wifi"

    private BigDecimal latitud;

    private BigDecimal longitud;

    /**
     * Mensaje informativo opcional, por ejemplo cuando la publicacion se
     * guarda como BORRADOR porque el anfitrion todavia no esta verificado
     * (escenario alternativo de US06).
     */
    private String advertencia;
}
