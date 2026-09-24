package com.wasimasi.api.dto;

import com.wasimasi.api.model.EstadoVerificacion;
import com.wasimasi.api.model.RolUsuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Datos de salida de un usuario, sin exponer la contrasena. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioResponseDTO {
    private Long id;
    private String nombre;
    private String correo;
    private RolUsuario rol;
    private String universidad;
    private EstadoVerificacion estadoVerificacion;
}
