package com.wasimasi.api.dto;

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

    /**
     * Mensaje informativo opcional, por ejemplo cuando la publicacion se
     * guarda como BORRADOR porque el anfitrion todavia no esta verificado
     * (escenario alternativo de US06).
     */
    private String advertencia;
}
