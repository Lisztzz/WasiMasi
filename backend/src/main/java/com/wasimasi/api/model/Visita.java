package com.wasimasi.api.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/** US16/US17/US19 - Solicitud de visita a una publicación. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "visitas")
public class Visita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // US16 - quién solicita (BUSCADOR)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "buscador_id")
    private Usuario buscador;

    // US17 - quién publica (ANFITRION) y confirma/rechaza
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "publicacion_id")
    private Publicacion publicacion;

    @Column(nullable = false)
    private LocalDateTime fechaHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoVisita estado;

    @Column(length = 500)
    private String comentario;           // mensaje del buscador

    @Column(length = 500)
    private String motivoRespuesta;      // motivo del rechazo (US17)

    @Column(nullable = false)
    private LocalDateTime creadaEn;

    private LocalDateTime actualizadaEn;
}
