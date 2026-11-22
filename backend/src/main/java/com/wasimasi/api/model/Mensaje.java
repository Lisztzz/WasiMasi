package com.wasimasi.api.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/** US12/US13 - Mensaje persistido dentro de una conversación. */
@Entity
@Table(name = "mensajes",
        indexes = @Index(name = "idx_mensajes_conversacion", columnList = "conversacion_id, id"),
        uniqueConstraints = @UniqueConstraint(name = "uk_mensaje_envio",
                columnNames = {"conversacion_id", "emisor_id", "cliente_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversacion_id", nullable = false)
    private Conversacion conversacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "emisor_id", nullable = false)
    private Usuario emisor;

    @Column(nullable = false, length = 1000)
    private String contenido;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaEnvio;

    @Column(updatable = false)
    private UUID clienteId;

    public Mensaje(Conversacion conversacion, Usuario emisor, String contenido) {
        this(conversacion, emisor, contenido, null);
    }

    public Mensaje(Conversacion conversacion, Usuario emisor, String contenido, UUID clienteId) {
        this.clienteId = clienteId;
        this.conversacion = conversacion;
        this.emisor = emisor;
        this.contenido = contenido;
        this.fechaEnvio = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
    }
}
