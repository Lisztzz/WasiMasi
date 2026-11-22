package com.wasimasi.api.mapper;

import com.wasimasi.api.dto.VisitaResponseDTO;
import com.wasimasi.api.model.Visita;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** US16/US17 - Convierte visitas en respuestas sin exponer entidades JPA. */
@Mapper(componentModel = "spring")
public interface VisitaMapper {

    @Mapping(target = "publicacionId", source = "publicacion.id")
    @Mapping(target = "zona", source = "publicacion.zona")
    @Mapping(target = "buscadorId", source = "buscador.id")
    @Mapping(target = "buscadorNombre", source = "buscador.nombre")
    @Mapping(target = "anfitrionNombre", source = "publicacion.anfitrion.nombre")
    VisitaResponseDTO toDto(Visita visita);
}
