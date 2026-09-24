package com.wasimasi.api.mapper;

import com.wasimasi.api.dto.UsuarioResponseDTO;
import com.wasimasi.api.model.Usuario;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {
    UsuarioResponseDTO toResponseDTO(Usuario usuario);
}
