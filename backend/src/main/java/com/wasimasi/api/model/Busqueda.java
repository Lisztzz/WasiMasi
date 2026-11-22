package com.wasimasi.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** US20 - Registro agregado y sin identidad personal de una búsqueda real (US08). */
@Entity
@Table(name = "busquedas")
@Getter
@NoArgsConstructor
public class Busqueda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String zona;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precioMin;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precioMax;

    @Column(nullable = false, updatable = false)
    private LocalDateTime creadaEn;

    public Busqueda(String zona, BigDecimal precioMin, BigDecimal precioMax) {
        this.zona = zona;
        this.precioMin = precioMin;
        this.precioMax = precioMax;
    }

    @PrePersist
    void alCrear() {
        if (creadaEn == null) {
            creadaEn = LocalDateTime.now();
        }
    }
}
