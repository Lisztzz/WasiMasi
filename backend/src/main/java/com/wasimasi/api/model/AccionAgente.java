package com.wasimasi.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * US22 - Acción que el agente de IA propone y que solo se ejecuta cuando el
 * usuario la confirma. Queda guardada como registro de lo que decidió el usuario.
 */
@Entity
@Table(name = "acciones_agente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccionAgente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoAccionAgente tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoAccionAgente estado = EstadoAccionAgente.PENDIENTE;

    /** Habitación sobre la que se propone la visita. */
    @Column(name = "publicacion_id", nullable = false)
    private Long publicacionId;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(length = 500)
    private String comentario;

    /** Visita creada al confirmar (null mientras no se confirme). */
    @Column(name = "visita_id")
    private Long visitaId;

    @Column(name = "creada_en", nullable = false, updatable = false)
    private LocalDateTime creadaEn;

    @Column(name = "resuelta_en")
    private LocalDateTime resueltaEn;

    @PrePersist
    void alCrear() {
        if (creadaEn == null) {
            creadaEn = LocalDateTime.now();
        }
    }
}
