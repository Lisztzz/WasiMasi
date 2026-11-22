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
 * Cubre US06 (publicar), US08 (buscar), US09 (condiciones negociables/no negociables)
 * y US11 (ver detalle).
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

    /** US15 - Habitación vinculada a una vivienda por su anfitrión. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vivienda_id")
    private Vivienda vivienda;

    /** US07 - Título opcional para conservar las publicaciones existentes. */
    private String titulo;

    /** US09 - Condiciones negociables/no negociables. */
    @Column(name = "mascotas_permitidas", nullable = true)
    private Boolean mascotasPermitidas;

    @Column(name = "genero_preferido", length = 10, nullable = true)
    private String generoPreferido; // 'M', 'F', 'Any'

    @Column(name = "servicios", length = 255, nullable = true)
    private String servicios; // Coma-separado: "agua,electricidad,wifi"

    /**
     * US09 - Claves de las condiciones que NO se pueden negociar ("mascotas,genero,servicios").
     * Null = publicacion anterior a US09: se siguen tratando todas como rigidas.
     */
    @Column(name = "condiciones_no_negociables", length = 60, nullable = true)
    private String condicionesNoNegociables;

    @Column(name = "latitud", precision = 10, scale = 6, nullable = true)
    private BigDecimal latitud;

    @Column(name = "longitud", precision = 10, scale = 6, nullable = true)
    private BigDecimal longitud;

    @PrePersist
    void alCrear() {
        this.fechaPublicacion = LocalDateTime.now();
        if (this.estado == null) {
            this.estado = EstadoPublicacion.BORRADOR;
        }
    }
}
