package com.wasimasi.api.dto;

import com.wasimasi.api.model.RolUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Datos de entrada para US01 - Registro como buscador o anfitrion.
 * Segun el rol, se espera codigoEstudiante+universidad (BUSCADOR)
 * o documentoIdentidad (ANFITRION).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistroRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato valido")
    private String correo;

    @NotBlank(message = "La contrasena es obligatoria")
    private String password;

    @NotNull(message = "El rol es obligatorio (BUSCADOR o ANFITRION)")
    private RolUsuario rol;

    /** Requerido solo si rol = BUSCADOR. */
    private String codigoEstudiante;

    /** Requerido solo si rol = BUSCADOR. */
    private String universidad;

    /** Requerido solo si rol = ANFITRION. */
    private String documentoIdentidad;
}
