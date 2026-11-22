package com.wasimasi.api.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** US15 - Identidad compartida por las habitaciones de una vivienda y su grupo. */
@Entity
@Table(name = "viviendas")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Vivienda {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "anfitrion_id", nullable = false)
    private Usuario anfitrion;

    @Column(nullable = false, length = 100)
    private String nombre;

    private LocalDateTime grupoCreadoEn;
    private LocalDateTime grupoCerradoEn;

    public Vivienda(Usuario anfitrion, String nombre) {
        this.anfitrion = anfitrion;
        this.nombre = nombre;
    }

    public void iniciarGrupo() {
        if (grupoCreadoEn == null) grupoCreadoEn = LocalDateTime.now();
    }

    public void cerrarGrupo() {
        if (grupoCerradoEn == null) grupoCerradoEn = LocalDateTime.now();
    }

    public boolean grupoCerrado() {
        return grupoCerradoEn != null;
    }
}
