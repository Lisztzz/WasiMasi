package com.wasimasi.api.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/** US15 - Mensaje de una vivienda, con reintentos idempotentes por emisor. */
@Entity
@Table(name = "mensajes_grupo",
        indexes = @Index(name = "idx_mensajes_grupo", columnList = "vivienda_id, id"),
        uniqueConstraints = @UniqueConstraint(name = "uk_mensaje_grupo_envio",
                columnNames = {"vivienda_id", "emisor_id", "cliente_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MensajeGrupo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vivienda_id", nullable = false)
    private Vivienda vivienda;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "emisor_id", nullable = false)
    private Usuario emisor;

    @Column(nullable = false, length = 1000)
    private String contenido;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaEnvio;

    @Column(nullable = false, updatable = false)
    private UUID clienteId;

    public MensajeGrupo(Vivienda vivienda, Usuario emisor, String contenido, UUID clienteId) {
        this.vivienda = vivienda;
        this.emisor = emisor;
        this.contenido = contenido;
        this.clienteId = clienteId;
        this.fechaEnvio = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
    }
}
