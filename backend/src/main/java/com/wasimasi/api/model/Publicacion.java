package com.wasimasi.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Publicacion de una habitacion disponible, hecha por un anfitrion.
 * Cubre US06 (publicar), US08 (buscar) y US11 (ver detalle).
 */
@Entity
@Table(name = "publicaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Publicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "anfitrion_id", nullable = false)
    private Usuario anfitrion;

    @Column(nullable = false)
    private String zona;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(length = 1000)
    private String descripcion;

    @ElementCollection
    @CollectionTable(name = "publicacion_fotos", joinColumns = @JoinColumn(name = "publicacion_id"))
    @Column(name = "url_foto")
    @Builder.Default
    private List<String> fotos = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoPublicacion estado = EstadoPublicacion.BORRADOR;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaPublicacion;

    @PrePersist
    void alCrear() {
        this.fechaPublicacion = LocalDateTime.now();
        if (this.estado == null) {
            this.estado = EstadoPublicacion.BORRADOR;
        }
    }
}
