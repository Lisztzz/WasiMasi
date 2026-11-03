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
 * token: JWT que se envia luego en el header "Authorization: Bearer <token>".
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDTO {
    private String token;
    private String tipoToken;
    private long expiraEnSegundos;
    private UsuarioResponseDTO usuario;
    private boolean requiereVerificacion;
}
