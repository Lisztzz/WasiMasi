package com.wasimasi.api.mapper;

import com.wasimasi.api.dto.PublicacionResponseDTO;
import com.wasimasi.api.model.Publicacion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PublicacionMapper {

    @Mapping(target = "anfitrionId", source = "anfitrion.id")
    @Mapping(target = "anfitrionNombre", source = "anfitrion.nombre")
    @Mapping(target = "anfitrionVerificado", expression = "java(publicacion.getAnfitrion().getEstadoVerificacion() == com.wasimasi.api.model.EstadoVerificacion.VERIFICADO)")
    PublicacionResponseDTO toResponseDTO(Publicacion publicacion);
}
