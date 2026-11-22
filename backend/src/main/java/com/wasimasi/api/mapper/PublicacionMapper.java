package com.wasimasi.api.mapper;

import com.wasimasi.api.dto.PublicacionResponseDTO;
import com.wasimasi.api.model.Publicacion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PublicacionMapper {

    @Mapping(source = "anfitrion.id", target = "anfitrionId")
    @Mapping(source = "anfitrion.nombre", target = "anfitrionNombre")
    @Mapping(target = "anfitrionVerificado",
            expression = "java(publicacion.getAnfitrion().getEstadoVerificacion() == com.wasimasi.api.model.EstadoVerificacion.VERIFICADO)")
    @Mapping(target = "advertencia", ignore = true)
    PublicacionResponseDTO toResponseDTO(Publicacion publicacion);
}
