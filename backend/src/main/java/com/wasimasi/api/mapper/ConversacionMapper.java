package com.wasimasi.api.mapper;

import com.wasimasi.api.dto.ConversacionResponseDTO;
import com.wasimasi.api.dto.MensajeResponseDTO;
import com.wasimasi.api.model.Conversacion;
import com.wasimasi.api.model.Mensaje;
import com.wasimasi.api.model.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ConversacionMapper {

    @Mapping(target = "id", source = "conversacion.id")
    @Mapping(target = "fechaCreacion", source = "conversacion.fechaCreacion")
    @Mapping(target = "interlocutorNombre", source = "interlocutor.nombre")
    ConversacionResponseDTO toResponseDTO(Conversacion conversacion, Usuario interlocutor);

    @Mapping(target = "emisorId", source = "emisor.id")
    MensajeResponseDTO toResponseDTO(Mensaje mensaje);
}
