package com.wasimasi.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Datos de entrada para US02 - Inicio de sesion. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {

    @NotBlank(message = "El correo es obligatorio")
    private String correo;

    @NotBlank(message = "La contrasena es obligatoria")
    private String password;
}
