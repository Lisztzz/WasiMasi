
package com.wasimasi.api.mapper;

import com.wasimasi.api.dto.PublicacionResponseDTO;
import com.wasimasi.api.model.Publicacion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** US06/US08/US11 - Convierte publicaciones en respuestas públicas seguras. */
@Mapper(componentModel = "spring")
public interface PublicacionMapper {

    @Mapping(target = "anfitrionId", source = "anfitrion.id")
    @Mapping(target = "anfitrionNombre", source = "anfitrion.nombre")
    @Mapping(target = "anfitrionVerificado",
            expression = "java(publicacion.getAnfitrion().getEstadoVerificacion() == com.wasimasi.api.model.EstadoVerificacion.VERIFICADO)")
    @Mapping(target = "advertencia", ignore = true)
    PublicacionResponseDTO toResponseDTO(Publicacion publicacion);
}
