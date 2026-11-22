package com.wasimasi.api.mapper;

import com.wasimasi.api.dto.GrupoResponseDTO;
import com.wasimasi.api.dto.MensajeGrupoResponseDTO;
import com.wasimasi.api.dto.ViviendaResponseDTO;
import com.wasimasi.api.model.MensajeGrupo;
import com.wasimasi.api.model.Vivienda;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GrupoMapper {
    ViviendaResponseDTO toViviendaDTO(Vivienda vivienda);

    @Mapping(target = "interlocutorNombre", source = "nombre")
    @Mapping(target = "fechaCreacion", source = "grupoCreadoEn")
    @Mapping(target = "cerrada", expression = "java(vivienda.grupoCerrado())")
    GrupoResponseDTO toGrupoDTO(Vivienda vivienda);

    @Mapping(target = "emisorId", source = "emisor.id")
    @Mapping(target = "emisorNombre", source = "emisor.nombre")
    MensajeGrupoResponseDTO toMensajeDTO(MensajeGrupo mensaje);
}
