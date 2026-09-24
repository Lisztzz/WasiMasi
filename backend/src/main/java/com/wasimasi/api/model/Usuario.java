package com.wasimasi.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Usuario de WasiMasi. Cubre US01 (registro), US02 (login) y sirve de base
 * para US03/US04/US05 (verificacion), aqui simulada.
 */
@Entity
@Table(name = "usuarios", uniqueConstraints = @UniqueConstraint(columnNames = "correo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String correo;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RolUsuario rol;

    /** Solo aplica a rol BUSCADOR (US01, US03). */
    private String codigoEstudiante;

    /** Solo aplica a rol BUSCADOR (US03). */
    private String universidad;

    /** Solo aplica a rol ANFITRION (US01, US04): numero de DNI. */
    private String documentoIdentidad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoVerificacion estadoVerificacion = EstadoVerificacion.PENDIENTE;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    void alCrear() {
        this.fechaCreacion = LocalDateTime.now();
        if (this.estadoVerificacion == null) {
            this.estadoVerificacion = EstadoVerificacion.PENDIENTE;
        }
    }
}
