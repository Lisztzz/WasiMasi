package com.wasimasi.api.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** US12 - Conversación directa entre un buscador y un anfitrión. */
@Entity
@Table(name = "conversaciones", uniqueConstraints = @UniqueConstraint(
        name = "uk_conversacion_directa", columnNames = {"buscador_id", "anfitrion_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Conversacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buscador_id", nullable = false)
    private Usuario buscador;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "anfitrion_id", nullable = false)
    private Usuario anfitrion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "publicacion_id", nullable = false)
    private Publicacion publicacion;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    private Long lecturaBuscadorId;
    private Long lecturaAnfitrionId;

    public Conversacion(Usuario buscador, Publicacion publicacion) {
        this.buscador = buscador;
        this.anfitrion = publicacion.getAnfitrion();
        this.publicacion = publicacion;
        this.fechaCreacion = LocalDateTime.now();
    }

    /** US13 - Conserva el mayor cursor confirmado por el participante. */
    public void registrarLectura(Long usuarioId, Long mensajeId) {
        if (buscador.getId().equals(usuarioId)) {
            lecturaBuscadorId = avanzar(lecturaBuscadorId, mensajeId);
        } else if (anfitrion.getId().equals(usuarioId)) {
            lecturaAnfitrionId = avanzar(lecturaAnfitrionId, mensajeId);
        }
    }

    private Long avanzar(Long lecturaActual, Long mensajeId) {
        return lecturaActual == null ? mensajeId : Math.max(lecturaActual, mensajeId);
    }
}
