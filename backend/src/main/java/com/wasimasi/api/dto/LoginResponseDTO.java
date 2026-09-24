package com.wasimasi.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Respuesta de US02 - Inicio de sesion.
 * requiereVerificacion=true simula el escenario alternativo:
 * la cuenta existe pero su verificacion (US03/US04) aun esta pendiente.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDTO {
    private UsuarioResponseDTO usuario;
    private boolean requiereVerificacion;
}
